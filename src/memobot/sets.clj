(ns memobot.sets
  (:use [memobot types]
        [clojure.set]))

(defn sadd-cmd
  "Add one or more members to a set"
  [k v]
  (let [s (eval k)]
    (if (not (contains? @s v))
      (do
        (swap! s #(conj % v))
        1)
      0)))

(defn scard-cmd
  "Get the number of members in a set"
  [k]
  (count k))

(defn sdiff-cmd
  "Subtract multiple sets"
  [k1 k2]
  (difference k1 k2))

(defn sinter-cmd
  "Intersect multiple sets"
  [k1 k2]
  (let [s1 (get-atom k1) 
        k2 (get-key (namespace k1) k2)
        s2 (if (exists? k2) (get-atom k2) nil)]
    
    (intersection s1 s2)))

(defn sismember-cmd
  "Determine if a given value is a member of a set"
  [k v]
  (if (contains? k v) 1 0))

(defn smembers-cmd
  "Get all the members in a set"
  [k]
  k)

(defn srem-cmd
  "Remove one or more members from a set"
  [k v]
  (let [s (eval k)]
    (if (contains? @s v)
      (do
        (swap! s #(disj % v))
        1)
      0)))

(defn sunion-cmd
  "Add multiple sets"
  [k1 k2]
  (let [s1 (get-atom k1) 
        k2 (get-key (namespace k1) k2)
        s2 (if (exists? k2) (get-atom k2) nil)]
    
    (union s1 s2)))

