(ns djoko.core-test
  (:require [clojure.test :refer [deftest is]]
            [djoko.core :as core]
            [djoko.game :as game]))

(deftest score-text-covers-tennis-scenarios
  (is (= "0-0" (game/score-text (game/new-game))))
  (is (= "40-30" (game/score-text {:player 3 :cpu 2})))
  (is (= "Deuce" (game/score-text {:player 3 :cpu 3})))
  (is (= "Advantage player" (game/score-text {:player 4 :cpu 3})))
  (is (= "Advantage cpu" (game/score-text {:player 5 :cpu 6}))))

(deftest new-game-includes-selected-difficulty
  (is (= :normal (:difficulty (game/new-game))))
  (is (= :easy (:difficulty (game/new-game :easy)))))

(deftest quit-options-are-available-in-prompts
  (is (= :quit (core/input->difficulty "q")))
  (is (= :quit (core/input->difficulty "quit")))
  (is (= :quit (core/input->style "q")))
  (is (= :quit (core/input->style "quit"))))

(deftest play-point-follows-real-game-rules
  (is (= {:player 1
          :cpu 0
          :difficulty :normal
          :last-point {:winner :player}}
         (game/play-point (game/new-game) {:winner :player})))
  (is (= :player
         (:winner (game/play-point {:player 3 :cpu 2} {:winner :player}))))
  (is (nil? (:winner (game/play-point {:player 3 :cpu 3} {:winner :player}))))
  (is (= "Advantage player"
         (game/score-text (game/play-point {:player 3 :cpu 3} {:winner :player}))))
  (is (= :cpu
         (:winner (game/play-point {:player 4 :cpu 5} {:winner :cpu})))))

(deftest cpu-style-reacts-to-score-context
  (is (= :balanced (game/choose-cpu-style {:player 0 :cpu 0})))
  (is (= :aggressive (game/choose-cpu-style {:player 3 :cpu 2})))
  (is (= :safe (game/choose-cpu-style {:player 2 :cpu 3}))))

(deftest difficulty-changes-shot-profile
  (let [trajectory (game/shot-trajectory (game/new-game) :player :balanced 1 (constantly 0.5))]
    (is (> (:error (game/shot-profile (game/new-game :hard) :player :balanced 1 trajectory))
           (:error (game/shot-profile (game/new-game :easy) :player :balanced 1 trajectory))))
    (is (> (:winner (game/shot-profile (game/new-game :easy) :player :balanced 1 trajectory))
           (:winner (game/shot-profile (game/new-game :hard) :player :balanced 1 trajectory))))
    (is (> (:winner (game/shot-profile (game/new-game :hard) :cpu :balanced 1 trajectory))
           (:winner (game/shot-profile (game/new-game :easy) :cpu :balanced 1 trajectory))))))

(deftest parabola-trajectory-stays-grounded-in-court-geometry
  (let [trajectory (game/shot-trajectory (game/new-game) :player :balanced 1 (constantly 0.5))]
    (is (> (:net-clearance trajectory) 0.0))
    (is (> (:landing-distance trajectory) game/net-position))
    (is (< (:landing-distance trajectory) game/court-length))
    (is (> (:apex trajectory) game/contact-height))))

(deftest commentary-adds-context-to-the-match-flow
  (is (= "Deuce. Margin is thin and shot quality matters more now."
         (core/score-context {:player 3 :cpu 3})))
  (is (= "Game point for player. The cpu has no room for a cheap miss."
         (core/score-context {:player 3 :cpu 2})))
  (is (= "Cpu read: sitting deeper and waiting for an error."
         (core/cpu-read {:player 2 :cpu 3})))
  (is (= "Hard: cpu gets cleaner ball-striking and player has less margin."
         (core/difficulty-copy :hard)))
  (is (= "\u001b[2J\u001b[H" core/clear-screen-escape))
  (is (= "Player took a grinding rally by striking first. The ball traveled with a low margin over the net and landed close to the baseline. Player: aggressive. Cpu: balanced."
         (core/point-summary {:winner :player
                              :event :winner
                              :shots 8
                              :trajectory {:net-clearance 0.3
                                           :landing-depth 0.96}
                              :player-style :aggressive
                              :cpu-style :balanced}))))

(deftest simulate-rally-resolves-from-shot-profile
  (let [rolls (atom [0.5 0.5 0.5
                     0.5 0.5 0.1])
        point (game/simulate-rally
               (game/new-game)
               :balanced
               (fn []
                 (let [roll (first @rolls)]
                   (swap! rolls rest)
                   roll)))]
    (is (= :player (:winner point)))
    (is (= :error (:event point)))
    (is (= :forced (:error-type point)))
    (is (= 2 (:shots point)))
    (is (= :balanced (:player-style point)))
    (is (= :balanced (:cpu-style point)))
    (is (> (get-in point [:trajectory :net-clearance]) 0.0))
    (is (> (get-in point [:trajectory :landing-depth]) 0.8))))
