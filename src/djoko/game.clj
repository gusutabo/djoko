(ns djoko.game)

(def point-labels ["0" "15" "30" "40"])

(def gravity 9.81)
(def contact-height 1.0)
(def court-length 23.77)
(def net-position (/ court-length 2))
(def net-height 0.914)

(def difficulties
  {:easy {:player {:error -0.04 :winner 0.03 :control 0.18}
          :cpu {:error 0.04 :winner -0.03 :control -0.18}}
   :normal {:player {:error 0.0 :winner 0.0 :control 0.0}
            :cpu {:error 0.0 :winner 0.0 :control 0.0}}
   :hard {:player {:error 0.03 :winner -0.02 :control -0.15}
          :cpu {:error -0.03 :winner 0.02 :control 0.15}}})

(def styles
  {:safe {:error 0.12 :winner 0.07}
   :balanced {:error 0.17 :winner 0.12}
   :aggressive {:error 0.24 :winner 0.18}})

(def style-physics
  {:safe {:speed 18.0 :angle 18.0}
   :balanced {:speed 19.0 :angle 14.0}
   :aggressive {:speed 20.0 :angle 11.0}})

(defn new-game
  ([] (new-game :normal))
  ([difficulty]
   {:player 0 :cpu 0 :difficulty difficulty}))

(defn opponent [side]
  (if (= side :player) :cpu :player))

(defn game-point? [state side]
  (let [own (side state)
        other ((opponent side) state)]
    (and (>= own 3) (>= (- own other) 1))))

(defn score-text [{:keys [player cpu winner]}]
  (cond
    winner
    (str (name winner) " wins the game")

    (and (>= player 3) (>= cpu 3) (= player cpu))
    "Deuce"

    (and (>= player 4) (= 1 (- player cpu)))
    "Advantage player"

    (and (>= cpu 4) (= 1 (- cpu player)))
    "Advantage cpu"

    :else
    (str (point-labels player) "-" (point-labels cpu))))

(defn choose-cpu-style [state]
  (cond
    (game-point? state :cpu) :safe
    (game-point? state :player) :aggressive
    (> (:player state) (:cpu state)) :aggressive
    (> (:cpu state) (:player state)) :balanced
    :else :balanced))

(defn noise [rng span]
  (* span (- (* 2 (rng)) 1)))

(defn flight-time [_ vy]
  (/ (+ vy (Math/sqrt (+ (* vy vy) (* 2 gravity contact-height))))
     gravity))

(defn height-at-distance [vx vy x]
  (let [t (/ x vx)]
    (- (+ contact-height (* vy t))
       (* 0.5 gravity t t))))

(defn shot-trajectory [state side style shots rng]
  (let [{:keys [speed angle]} (style-physics style)
        {:keys [control]} (get-in difficulties [(:difficulty state :normal) side])
        pressure (if (game-point? state (opponent side)) 0.35 0.0)
        fatigue (* 0.03 (dec shots))
        speed-variance (max 0.2 (- 0.85 control fatigue))
        angle-variance (max 0.6 (- 2.1 (* 4 control) (* 0.08 shots)))
        actual-speed (+ speed (noise rng speed-variance))
        actual-angle (+ angle pressure (noise rng angle-variance))
        radians (/ (* actual-angle Math/PI) 180.0)
        vx (* actual-speed (Math/cos radians))
        vy (* actual-speed (Math/sin radians))
        total-flight (flight-time vx vy)
        landing-distance (* vx total-flight)
        net-clearance (- (height-at-distance vx vy net-position) net-height)
        apex (+ contact-height (/ (* vy vy) (* 2 gravity)))]
    {:speed actual-speed
     :angle actual-angle
     :landing-distance landing-distance
     :landing-depth (/ landing-distance court-length)
     :net-clearance net-clearance
     :apex apex}))

(defn trajectory-fault [trajectory]
  (cond
    (<= (:net-clearance trajectory) 0.0) :net
    (> (:landing-distance trajectory) court-length) :long
    :else nil))

(defn shot-profile [state side style shots trajectory]
  (let [{base-error :error
         base-winner :winner} (styles style)
        {difficulty-error :error
         difficulty-winner :winner} (get-in difficulties [(:difficulty state :normal) side])
        pressure (if (game-point? state (opponent side)) 0.04 0.0)
        rally-bonus (* 0.01 (min 8 shots))
        trajectory-risk (max 0.0 (* 0.32 (- 0.45 (:net-clearance trajectory))))
        trajectory-reward (max 0.0 (* 0.3 (- (:landing-depth trajectory) 0.8)))
        hanging-ball (max 0.0 (* 0.06 (- (:apex trajectory) 3.4)))
        error-rate (-> (+ base-error
                          pressure
                          (* 0.012 (dec shots))
                          difficulty-error
                          trajectory-risk
                          hanging-ball)
                       (max 0.05)
                       (min 0.55))
        winner-rate (-> (+ base-winner
                           rally-bonus
                           (if (game-point? state side) 0.02 0.0)
                           difficulty-winner
                           trajectory-reward
                           (* 0.08 (max 0.0 (- 0.35 (:net-clearance trajectory)))))
                        (max 0.03)
                        (min (- 0.9 error-rate)))]
    {:error error-rate
     :winner winner-rate}))

(defn simulate-rally
  ([state player-style]
   (simulate-rally state player-style rand))
  ([state player-style rng]
   (let [cpu-style (choose-cpu-style state)]
     (loop [side :player
            shots 1]
       (let [style (if (= side :player) player-style cpu-style)
             trajectory (shot-trajectory state side style shots rng)
             fault (trajectory-fault trajectory)
             {:keys [error winner]} (shot-profile state side style shots trajectory)
             roll (rng)]
         (cond
           fault
           {:winner (opponent side)
            :event :error
            :error-type fault
            :shots shots
            :trajectory trajectory
            :player-style player-style
            :cpu-style cpu-style}

           (< roll error)
           {:winner (opponent side)
            :event :error
            :error-type :forced
            :shots shots
            :trajectory trajectory
            :player-style player-style
            :cpu-style cpu-style}

           (< roll (+ error winner))
            {:winner side
            :event :winner
            :shots shots
            :trajectory trajectory
            :player-style player-style
            :cpu-style cpu-style}

           :else
           (recur (opponent side) (inc shots))))))))

(defn play-point [state point]
  (if (:winner state)
    state
    (let [winner (:winner point)
          next-state (update state winner inc)
          player (:player next-state)
          cpu (:cpu next-state)]
      (cond-> next-state
        (and (>= player 4) (>= (- player cpu) 2)) (assoc :winner :player)
        (and (>= cpu 4) (>= (- cpu player) 2)) (assoc :winner :cpu)
        true (assoc :last-point point)))))

(defn play-rally [state player-style]
  (play-point state (simulate-rally state player-style)))
