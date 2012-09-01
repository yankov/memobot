(ns memobot.hashes
  (:use [memobot types]))

; TODO:
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

