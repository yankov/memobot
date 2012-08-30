(ns memobot.strings)

;TODO:
; incr
; decr
; incrby
; decrby
; setnx 
; strlen

(defn fix-type [v]
  (try 
    (Integer/parseInt v)
  (catch NumberFormatException e v)))

(defn set-cmd 
  "Sets the value of a key"
  [db k v]
  (intern db (symbol k) (atom (fix-type v)))
  [:just-ok])

(defn get-cmd
  "Get the value of a key"
  [db k]
  (if (ns-resolve db (symbol k))
    [:ok (deref (eval (symbol (str db "/" k))))]
    [:nokeyerr]))

(defn incr-cmd
  "Increment the integer value of a key by one"
  [db k]
  (try 
    (prn (eval (symbol (str db "/" k))))
    (swap! (eval (symbol (str db "/" k))) inc)
    (catch ClassCastException e (do (prn "caught exception: " (.getMessage e)) [:nointerr]))))