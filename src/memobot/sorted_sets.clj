(ns memobot.sorted-sets
  (:use [memobot types]))

;TODO
; zcount
; zrangebyscore
; zrank
; zrem
; zrevrange
; zscore 

(defn sort-map
  "Sort a given map"
  [m dir]
  (into (sorted-map-by (fn [key1 key2]
   (* dir (compare [(get m key2) key2]
            [(get m key1) key1]) ) )) m))

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

(defn zincrby-cmd
  "Increment the score of a member in a sorted set"
  [db k increment member]
  (if (exists? db k)
    (let [ck (get-atom db k)
          member (keyword member)]
      (swap! ck #(assoc % member (+ (get @ck member 0) (fix-type increment))))
      [:ok (get @ck member)])
    (do 
      (intern db (symbol k) (atom {(keyword member) (fix-type increment)}))
      [:ok increment])))

(defn zrange-cmd
  "Return a range of members in a sorted set, by index"
  ([db k start end] (zrange-cmd db k start end "noscores"))
  ([db k start end scores] 
  (if (exists? db k)
    (let [ck (get-atom db k)]
      (if (map? @ck)
        (let [zset (drop (fix-type start) (take (fix-type end) (sort-map @ck 1)))]
          (if (= scores "noscores")
            [:ok (map #(name %) (keys zset))]  
            [:ok (flatten (map #(list (name (key %)) (val %) ) zset))]))
        [:wrongtypeerr]))
    [:emptymultibulk])))    

;TODO: DRY it up
(defn zrevrange-cmd
  "Return a range of members in a sorted set, by index, with scores ordered from high to low"
  ([db k start end] (zrange-cmd db k start end "noscores"))
  ([db k start end scores] 
  (if (exists? db k)
    (let [ck (get-atom db k)]
      (if (map? @ck)
        (let [zset (drop (fix-type start) (take (fix-type end) (sort-map @ck -1)))]
          (if (= scores "noscores")
            [:ok (map #(name %) (keys zset))]  
            [:ok (flatten (map #(list (name (key %)) (val %) ) zset))]))
        [:wrongtypeerr]))
    [:emptymultibulk])))    




