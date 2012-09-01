(ns memobot.hashes
  (:use [memobot types]))

; TODO:
; hdel
; hget
; hgetall
; hexists
; hincrby
; hkeys
; hlen
; hsetnx
; hvals

(defn hset-cmd 
  "Set the string value of a hash field"
  [db k f v]
  (let [ck (symbol (str db "/" k))]
      (if (not (resolve ck))
        (do 
          (intern db (symbol k) (atom {(keyword f) (fix-type v)}))
          [:cone])
        (if (map? (deref (eval ck))) 
          (do 
            (swap! (eval ck) #(assoc % (keyword f) (fix-type v)))
            [:cone])
          [:wrongtypeerr]))))

(defn hget-cmd
  "Get the value of a hash field"
  [db k f]
  (if (ns-resolve db (symbol k))
    (let [ck (deref (eval (symbol (str db "/" k))))]
      (if (map? ck)
        (let [v ((keyword f) ck)]
          (if (nil? v)
            [:nokeyerr]
            [:ok v]))
        [:wrongtypeerr]))
    [:nokeyerr]))