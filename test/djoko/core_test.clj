(ns djoko.core-test
  (:require [clojure.test :refer :all]
            [djoko.core :refer :all]))

(deftest test-next-point
  (testing "progressão de pontos"
    (is (= 15 (next-point 0)))
    (is (= 30 (next-point 15)))
    (is (= 40 (next-point 30)))
    (is (= 40 (next-point 40)))))

(deftest test-play-point
  (testing "p1 marca ponto"
    (is (= {:p1 15 :p2 0}
           (play-point {:p1 0 :p2 0} :p1))))

  (testing "p2 marca ponto"
    (is (= {:p1 0 :p2 15}
           (play-point {:p1 0 :p2 0} :p2))))

  (testing "p1 vence o game"
    (is (= {:winner :p1}
           (play-point {:p1 40 :p2 30} :p1))))

  (testing "p2 vence o game"
    (is (= {:winner :p2}
           (play-point {:p1 30 :p2 40} :p2)))))