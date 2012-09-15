(ns memobot.strings
  (:use [memobot types]))

(defn set-cmd 
  "Sets the value of a key"
  [k v]
  v)

(defn setnx-cmd 
  "Set the value of a key, only if the key does not exist"
  [k v]
  (if (= k :new) v k))

(defn get-cmd
  "Get the value of a key"
  [k]
  k)
  
(defn strlen-cmd
  "Get the length of the value stored in a key"
  [k]
  (.length (str k)))

(defn incr-cmd
  "Increment the integer value of a key by one"
  [k]
  (inc k))

(defn decr-cmd
  "Decrement the integer value of a key by one"
  [k]
  (dec k))

(defn incrby-cmd
  "Increment the integer value of a key by the given amount"
  [k v]
  (+ k v))

(defn decrby-cmd
  "Decrement the integer value of a key by the given amount"
  [k v]
  (- k v))


