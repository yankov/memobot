(ns memobot.sorted-sets
  (:use [memobot types]))

; TODO:
; zcount
; zrangebyscore
; zrank
; zrevrank

(defn sort-map
  "Sort a given map"
  [m dir]
  (into (sorted-map-by (fn [key1 key2]
   (* dir (compare [(get m key2) key2]
            [(get m key1) key1]) ) )) m))

(defn zadd-cmd
  "Add one or more members to a sorted set, or update its score if it already exists"
  [k score member]
  (assoc k (keyword member) score))
  
(defn zcard-cmd
  "Get the number of members in a sorted set"
  [k]
  (count k))

(defn zincrby-cmd
  "Increment the score of a member in a sorted set"
  [k increment member]
  (assoc k (keyword member) (+ (get k (keyword member) 0) increment)))

(defn zrange-cmd
  "Return a range of members in a sorted set, by index"
  ([k start end] (zrange-cmd k start end "noscores"))
  ([k start end scores] 
    (let [zset (drop start (take end (sort-map k 1)))]
      (if (= scores "noscores")
        (map #(name %) (keys zset))  
        (flatten (map #(list (name (key %)) (val %) ) zset))))))

;TODO: DRY it up
(defn zrevrange-cmd
  "Return a range of members in a sorted set, by index, with scores ordered from high to low"
  ([k start end] (zrange-cmd k start end "noscores"))
  ([k start end scores] 
    (let [zset (drop start (take end (sort-map k -1)))]
      (if (= scores "noscores")
        (map #(name %) (keys zset))  
        (flatten (map #(list (name (key %)) (val %) ) zset))))))


; (defn zrank-cmd
;   "Determine the index of a member in a sorted set"
;   [db k member]
;   (if (exists? db k)
;     (let [zset (get-atom db k)]
;       (if (map? @ck) 
;         [:ok ]
;         [:wrongtypeerr]))
;     [:nokeyerr]))

(defn zrem-cmd
  "Remove one or more members from a sorted set"
  [k member]
  (dissoc k (keyword member)))

(defn zscore-cmd
  "Get the score associated with the given member in a sorted set"
  [k member]
  (get k (keyword member) nil))