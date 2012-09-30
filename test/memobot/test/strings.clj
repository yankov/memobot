(ns memobot.test.strings
  (:use [memobot strings])
  (:use [clojure.test]))

(deftest strings-test
  (testing "set"
    (is (= (set-cmd 'k 10) 10)))
  (testing "setnx"
    (testing "when key doesn't exist"
      (is (= (setnx-cmd :new 10) 10)))
    (testing "when key exists"
      (is (= (setnx-cmd 'k 10) 'k))))
  (testing "strlen"
    (is (= (strlen-cmd "robot") 5)))
    (is (= (strlen-cmd 6) 1))
  (testing "incr"
    (is (= (incr-cmd 10) 11)))
  (testing "decr"
    (is (= (decr-cmd 10) 9)))
  (testing "incrby"
    (is (= (incrby-cmd 10 5) 15)))
  (testing "decrby"
    (is (= (decrby-cmd 10 5) 5))))
  

