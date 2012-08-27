(ns memobot.strings)

;TODO:
; incr
; decr
; incrby
; decrby
; setnx 
; strlen

(defn set-key 
  "Sets the value of a key"
  [db k v]
  (intern db (symbol k) (atom v))
  (prn "OK"))

(defn get-key 
  "Get the value of a key"
  [db k]
  (if (ns-resolve db (symbol k))
    (deref (eval (symbol (str db "/" k))))
    nil))
