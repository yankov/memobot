(ns memobot.types)

(defn fix-type [v]
  (try 
    (Integer/parseInt v)
  (catch NumberFormatException e v)))

(defn exists? 
  [db k]
  (ns-resolve db (symbol k)))

(defn get-atom
  [db k]
  (eval (symbol (str db "/" k))))