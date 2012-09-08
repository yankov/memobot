(ns memobot.sorted-sets
  (:use [memobot types]))

;TODO
; zcard
; zcount
; zincrby
; zrange
; zrangebyscore
; zrank
; zrem
; zrevrange
; zscore 

(defn zadd-cmd
  "Add one or more members to a sorted set, or update its score if it already exists"
  [db k score member]
  (if (not (exists? db k))
    (do
    (intern db (symbol k) (atom {(keyword member) (fix-type score)}))
      [:cone])
    (let [ck (get-atom db k)]
      (if (map? @ck) 
        (do 
          (swap! (eval ck) #(assoc % (keyword member) (fix-type score)))
          [:cone])
        [:wrongtypeerr]))))
  
(defn zcard-cmd
  "Get the number of members in a sorted set"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:int (count @ck)])
    [:czero]))


; (into (sorted-map-by (fn [key1 key2]
;    (compare [(get m key2) key2]
;             [(get m key1) key1]))) m)
