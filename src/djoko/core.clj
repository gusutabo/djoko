(ns djoko.core)

(def points [0 15 30 40])

(defn next-point 
  [p]
  (let [idx (.indexOf points p)]
    (if (< idx 3)
      (nth points (inc idx))
      40)))

(defn play-point [state winner]
  (let [{:keys [p1 p2]} state]
    (cond
      (:winner state)
      state

      (and (= winner :p1) (= p1 40))
      {:winner :p1}
      
      (and (= winner :p2) (= p2 40))
      {:winner :p2}
      
      (= winner :p1)
      (update state :p1 next-point)

      (= winner :p2)
      (update state :p2 next-point))))

(defn player-action 
  []
  (println "Escolha: (d) defensiva ? (a) atacar")
  (read-line))

(defn decide-point [action]
  (let [r (rand)]
    (cond
      (= action "d")
      (if (< r 0.6) :p1 :p2)

      (= action "a")
      (if (< r 0.4) :p1 :p2)

      :else
      :p2)))

(defn play-game []
  (loop [state {:p1 0 :p2 0}]
    (println "Placar:" state)
    
    (if (:winner state)
      (do 
        (println "Vencedor do game:" (:winner state))
        state)
      
      (let [action (player-action)
            winner (decide-point action)]
            (println "Ponto para:" winner)
            (recur (play-point state winner))))))

(defn -main
  "I don't do a whole lot."
  [& args]
  (play-game))
