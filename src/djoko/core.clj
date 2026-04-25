(ns djoko.core
  (:require [clojure.string :as str]
            [djoko.game :as game]))

(def clear-screen-escape "\u001b[2J\u001b[H")

(def input->style
  {"s" :safe
   "d" :safe
   "b" :balanced
   "a" :aggressive
   "q" :quit
   "quit" :quit})

(def input->difficulty
  {"e" :easy
   "n" :normal
   "h" :hard
   "q" :quit
   "quit" :quit})

(def difficulty-copy
  {:easy "Easy: player gets a little more margin and cpu gives away more."
   :normal "Normal: balanced match conditions."
   :hard "Hard: cpu gets cleaner ball-striking and player has less margin."})

(def style-copy
  {:safe "Safe: absorb pace and extend the rally."
   :balanced "Balanced: trade margin for pace."
   :aggressive "Aggressive: press for a short point."})

(defn clear-screen []
  (print clear-screen-escape)
  (flush))

(defn prompt-choice [render options]
  (loop [feedback nil]
    (clear-screen)
    (render feedback)
    (if-let [choice (options (some-> (read-line) str/trim str/lower-case))]
      choice
      (recur "Invalid option. Try again."))))

(defn score-context [state]
  (cond
    (:winner state)
    (str/capitalize (game/score-text state))

    (and (game/game-point? state :player) (game/game-point? state :cpu))
    "Everything is live here. One swing changes the game."

    (game/game-point? state :player)
    "Game point for player. The cpu has no room for a cheap miss."

    (game/game-point? state :cpu)
    "Game point for cpu. Player needs a composed response."

    (= "Deuce" (game/score-text state))
    "Deuce. Margin is thin and shot quality matters more now."

    (= (:player state) (:cpu state))
    "Level score. No one has the edge yet."

    (> (:player state) (:cpu state))
    "Player is ahead. A solid point here would tighten control."

    :else
    "Cpu is ahead. Player needs to disrupt the rhythm."))

(defn cpu-read [state]
  (case (game/choose-cpu-style state)
    :safe "Cpu read: sitting deeper and waiting for an error."
    :balanced "Cpu read: holding a neutral court position."
    :aggressive "Cpu read: stepping in and looking to take time away."))

(defn rally-shape [shots]
  (cond
    (= shots 1) "a one-shot point"
    (<= shots 3) "a short exchange"
    (<= shots 6) "a measured rally"
    :else "a grinding rally"))

(defn clearance-read [net-clearance]
  (cond
    (< net-clearance 0.15) "just above the tape"
    (< net-clearance 0.45) "with a low margin over the net"
    :else "with heavy net clearance"))

(defn landing-read [landing-depth]
  (cond
    (< landing-depth 0.78) "short in the court"
    (< landing-depth 0.93) "through the middle third"
    :else "close to the baseline"))

(defn point-summary [{:keys [winner event shots player-style cpu-style error-type trajectory]}]
  (str (str/capitalize (name winner))
       " took "
       (rally-shape shots)
       " by "
       (if (= event :winner)
         "striking first"
         (case error-type
           :net "forcing a net error"
           :long "drawing a long miss"
           "drawing the mistake"))
       ". The ball traveled "
       (clearance-read (:net-clearance trajectory))
       " and landed "
       (landing-read (:landing-depth trajectory))
       ". Player: "
       (name player-style)
       ". Cpu: "
       (name cpu-style)
       "."))

(defn point-follow-up [{:keys [winner shots trajectory]}]
  (cond
    (>= shots 8)
    "That rally had weight on both sides."

    (> (:apex trajectory) 4.5)
    "The arc bought time, but it left the ball hanging."

    (> (:landing-depth trajectory) 0.95)
    "That ball pushed deep and rushed the reply."

    (= winner :player)
    "Player dictated the finish."

    :else
    "Cpu absorbed the pressure and came through."))

(defn render-difficulty-screen [feedback]
  (println "Choose difficulty:")
  (println)
  (println "  (e) Easy")
  (println "  (n) Normal")
  (println "  (h) Hard")
  (println "  (q) Quit")
  (when feedback
    (println)
    (println feedback)))

(defn render-game-screen [state messages feedback]
  (println "Djoko")
  (println)
  (println "Difficulty:" (str/capitalize (name (:difficulty state))))
  (println "Score:" (game/score-text state))
  (when (seq messages)
    (println)
    (doseq [line messages]
      (println line)))
  (if (:winner state)
    (do
      (println)
      (println (str "Game, " (str/capitalize (name (:winner state))) ".")))
    (do
      (println)
      (println (score-context state))
      (println (cpu-read state))
      (println)
      (println "Choose your shot:")
      (println "  (s) Safe")
      (println "  (b) Balanced")
      (println "  (a) Aggressive")
      (println "  (q) Quit")))
  (when feedback
    (println)
    (println feedback)))

(defn play-game []
  (let [difficulty (prompt-choice render-difficulty-screen input->difficulty)]
    (if (= difficulty :quit)
      (do
        (clear-screen)
        (println "Match aborted.")
        {:quit true})
      (loop [state (game/new-game difficulty)
             messages [(difficulty-copy difficulty)]]
        (if (:winner state)
          (do
            (clear-screen)
            (render-game-screen state messages nil)
            state)
          (let [style (prompt-choice #(render-game-screen state messages %) input->style)]
            (if (= style :quit)
              (do
                (clear-screen)
                (render-game-screen state (conj messages "Match ended by player.") nil)
                {:quit true :state state})
              (let [next-state (game/play-rally state style)]
                (recur next-state
                       [(style-copy style)
                        (point-summary (:last-point next-state))
                        (point-follow-up (:last-point next-state))])))))))))

(defn -main
  "I don't do a whole lot."
  [& _]
  (play-game))
