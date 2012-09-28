(ns memobot.test.lists
  (:use [memobot lists]
        [clojure.test]
        [clojure.data.finger-tree]))

(deftest lists-test
  (testing "lindex"
    (let [l (counted-double-list 1 2 3)]
      (is (= (lindex-cmd l 1) 2))
      (is (= (lindex-cmd l 2) 3))
      (is (= (lindex-cmd l 4) 0)))))
