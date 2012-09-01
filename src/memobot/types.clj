(ns memobot.types)

(defn fix-type [v]
  (try 
    (Integer/parseInt v)
  (catch NumberFormatException e v)))

