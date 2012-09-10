(ns memobot.strings
  (:use [memobot types]))

(defn set-cmd 
  "Sets the value of a key"
  [k v]
  (intern (symbol (namespace k)) (symbol (name k)) (atom (fix-type v)))
  [:just-ok])

(defn setnx-cmd 
  "Set the value of a key, only if the key does not exist"
  [k v]
  (if (not (exists? k))
    (do 
       (set-cmd k v)
       [:cone])
    [:czero]))

(defn get-cmd
  "Get the value of a key"
  [k]
  [:ok k])
  
(defn strlen-cmd
  "Get the length of the value stored in a key"
  [k]
  [:int (.length k)])

(defn incr-cmd
  "Increment the integer value of a key by one"
  [k]
  [:int (swap! (eval k) inc)])

(defn decr-cmd
  "Decrement the integer value of a key by one"
  [k]
  [:int (swap! (eval k) dec)])

(defn incrby-cmd
  "Increment the integer value of a key by the given amount"
  [k v]
  [:int (swap! (eval k) #(+ % (fix-type v)))])

(defn decrby-cmd
  "Decrement the integer value of a key by the given amount"
  [k v]
  [:int (swap! (eval k) #(- % (fix-type v)))])


