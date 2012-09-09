(ns memobot.types)

(defn fix-type [v]
  (try 
    (Integer/parseInt v)
  (catch NumberFormatException e v)))

(defn exists? 
  ([k] (resolve k))
  ([db k] (ns-resolve db (symbol k))))

(defn get-key
  [db k]
  (symbol (str db "/" k)))

(defn get-atom
  [db k]
  (eval (get-key db k)))