(ns memobot.lists
  (:use [memobot types] 
        [clojure.data.finger-tree]))

(defn lindex-cmd
  "Get an element from a list by its index"
  [k i]
  (nth k i 0))

(defn lpush-cmd
  "Prepend one or multiple values to a list"
  [k v]
  (consl k v))

(defn lpushx-cmd
  "Prepend one or multiple values to a list"
  [k v]
  (if (= k :new) (counted-double-list v) k))
  
(defn llen-cmd
  "Get the length of a list"
  [k]
  (count k))

(defn lpop-cmd
  "Remove and get the first element in a list"
  [k]
  (let [l (eval k)
        e (first @l)]
    (if (> (count @l) 1)
      (swap! l #(next %))
      (del-cmd k))
    e))

(defn lrange-cmd
  "Get a range of elements from a list"
  [k start end]
  (drop start (take end k)))

(defn rpush-cmd
  "Append one or multiple values to a list"
  [k v]
  (conj k v))

(defn rpushx-cmd
  "Append one value to a list"
  [k v]
  (lpushx-cmd k v))

(defn rpop-cmd
  "Remove and get the first element in a list"
  [k]
  (let [l (eval k)
        e (peek @l)]
    (if (> (count @l) 1)
      (swap! l #(pop %))
      (del-cmd k))
    e))

(defn lset-cmd
  "Set the value of an element in a list by its index"
  [k i v]
  (assoc k i v))
