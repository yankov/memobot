(ns memobot.sets
  (:use [memobot types]
        [clojure.set]))

;TODO
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

;TODO: add support for more then 2 sets
(defn sdiff-cmd
  "Subtract multiple sets"
  [db k1 k2]
  (if (and (exists? db k1) (exists? db k2))
    (let [s1 (get-atom db k1)
          s2 (get-atom db k2)]
       (if (and (set? @s1) (set? @s2))
         [:ok (difference @s1 @s2)]
         [:wrongtypeerr]))
    [:emptymultibulk]))

(defn sinter-cmd
  "Intersect multiple sets"
  [db k1 k2]
  (if (and (exists? db k1) (exists? db k2))
    (let [s1 (get-atom db k1)
          s2 (get-atom db k2)]
       (if (and (set? @s1) (set? @s2))
         [:ok (intersection @s1 @s2)]
         [:wrongtypeerr]))
    [:emptymultibulk]))

(defn sismember-cmd
  "Determine if a given value is a member of a set"
  [db k v]
  (if (exists? db k)
    (let [s (get-atom db k)
          value (fix-type v)]
      [:int (if (contains? @s value) 1 0)])
    [:czero]))

(defn smembers-cmd
  "Get all the members in a set"
  [db k]
  (if (exists? db k)
    (let [s (get-atom db k)]
      (if (set? @s)
        [:ok @s]
        [:wrongtypeerr]))
    [:emptymultibulk]))    


