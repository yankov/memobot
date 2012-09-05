(ns memobot.sets
  (:use [memobot types]))

;TODO
; scard
; sdiff
; sinter
; sismember
; smembers
; smove
; spop
; srandmember
; srem 
; sunion

(defn sadd-cmd
  "Add one or more members to a set"
  [db k v]
  (if (not (exists? db k))
    (do
      (intern db (symbol k) (atom (conj #{} (fix-type v))))
      [:cone])
    (let [ck (get-atom db k)
          value (fix-type v)]
      (if (set? @ck) 
        (if (not (contains? @ck value))
          (do
            (swap! ck #(conj % value))
            [:cone])
          [:czero])
        [:wrongtypeerr]))))

(defn scard-cmd
  "Get the number of members in a set"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:int (count @ck)])
    [:czero]))