(ns memobot.hashes
  (:use [memobot types]))

(defn hset-cmd 
  "Set the string value of a hash field"
  [k f v]
  (assoc k (keyword f) v))

(defn hget-cmd
  "Get the value of a hash field"
  [k f]
  ((keyword f) k nil))

(defn hgetall-cmd
  "Get all the fields and values in a hash"
  [db k]
  (if (ns-resolve db (symbol k))
    (let [ck (deref (eval (symbol (str db "/" k))))]
      (if (map? ck)
        [:ok (flatten (map #(list (name (key %)) (val %) )  ck))]
        [:wrongtypeerr]))
    [:nokeyerr]))

(defn hdel-cmd
  "Delete one or more hash fields"
  [db k f]
  (if (exists? db k) 
    (let [ck (get-atom db k)]
      (if (contains? @ck (keyword f))
        (do 
          (swap! ck #(dissoc % (keyword f)))
          [:cone])
        [:czero]))
    [:czero]))

(defn hexists-cmd
  "Determine if a hash field exists"
  [db k f]
  (if (exists? db k) 
    (let [ck (get-atom db k)]
      (if (contains? @ck (keyword f))
        [:cone]
        [:czero]))
    [:czero]))

(defn hkeys-cmd
  "Get all the fields in a hash"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:ok (map #(name %) (keys @ck))])
    [:emptymultibulk]))

(defn hlen-cmd
  "Get the number of fields in a hash"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:ok (count (keys @ck))])
    [:czero]))

(defn hsetnx-cmd
  "Set the value of a hash field, only if the field does not exist"
  [db k f v]
  (if (not= (hexists-cmd db k f) [:cone])
    (do
      (hset-cmd db k f v)
      [:cone])
    [:czero]))

(defn hvals-cmd
  "Get all the values in a hash"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:ok (map #(str %) (vals @ck))])
      [:emptymultibulk]))