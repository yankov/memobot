(ns memobot.hashes
  (:use [memobot types]))

(defn hset-cmd 
  "Set the string value of a hash field"
  [k f v]
  (assoc k (keyword f) v))

(defn hget-cmd
  "Get the value of a hash field"
  [k f]
  ((keyword f) k nil))

(defn hgetall-cmd
  "Get all the fields and values in a hash"
  [k]
  (flatten (map #(list (name (key %)) (val %) )  k)))

(defn hdel-cmd
  "Delete one or more hash fields"
  [k f]
  (dissoc k (keyword f)))

(defn hexists-cmd
  "Determine if a hash field exists"
  [k f]
  (if (contains? k (keyword f)) 1 0))

(defn hkeys-cmd
  "Get all the fields in a hash"
  [k]
  (map #(name %) (keys k)))

(defn hlen-cmd
  "Get the number of fields in a hash"
  [k]
  (count (keys k)))

(defn hsetnx-cmd
  "Set the value of a hash field, only if the field does not exist"
  [k f v]
  (if (not (hexists-cmd k f))
    (hset-cmd k f v)
    k))

(defn hvals-cmd
  "Get all the values in a hash"
  [k]
  (map #(str %) (vals k)))
