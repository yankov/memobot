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
  (let [ck (symbol (str db "/" k))]
    (try 
      (if (not (resolve ck))
        (set-cmd db k "0"))
      [:int (swap! (eval ck) inc)]
      (catch ClassCastException e (do (prn "caught exception: " (.getMessage e)) [:nointerr])))))

(defn decr-cmd
  "Decrement the integer value of a key by one"
  [db k]
  (let [ck (symbol (str db "/" k))]
    (try 
      (if (not (resolve ck))
        (set-cmd db k "0"))
      [:int (swap! (eval ck) dec)]
      (catch ClassCastException e (do (prn "caught exception: " (.getMessage e)) [:nointerr])))))
