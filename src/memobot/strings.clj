(ns memobot.strings)

;TODO:
; incr
; decr
; incrby
; decrby
; setnx 
; strlen

(defn set-cmd 
  "Sets the value of a key"
  [db k v]
  (intern db (symbol k) (atom v))
  [:just-ok])

(defn get-cmd
  "Get the value of a key"
  [db k]
  (if (ns-resolve db (symbol k))
    [:ok (deref (eval (symbol (str db "/" k))))]
    [:nokeyerr]))
