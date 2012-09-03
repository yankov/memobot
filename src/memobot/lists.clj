(ns memobot.lists
  (:use [memobot types] 
        [clojure.data.finger-tree]))

;TODO:
; lrem
; lset
; ltrim
; rpop
; rpush
; rpushx

(defn counted-list?
  [v]
  (= (type v) clojure.data.finger_tree.CountedDoubleList))

(defn lindex-cmd
  "Get an element from a list by its index"
  [db k i]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:ok (nth @ck (fix-type i) nil)])
    [:nokeyerr]))

(defn lpush-cmd
  "Prepend one or multiple values to a list"
  [db k v]
  (if (not (exists? db k))
    (do
      (intern db (symbol k) (atom (counted-double-list (fix-type v))))
      [:cone])
    (let [ck (get-atom db k)]
      (if (counted-list? @ck) 
        (do 
          (swap! ck #(consl % (fix-type v)))
          [:int (count @ck)])
        [:wrongtypeerr]))))

(defn lpushx-cmd
  "Prepend one or multiple values to a list"
  [db k v]
  (if (exists? db k)
    (lpush-cmd db k v)
    [:czero]))
  

(defn llen-cmd
  "Get the length of a list"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:int (count @ck)])
    [:czero]))

(defn lpop-cmd
  "Remove and get the first element in a list"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      (if (counted-list? @ck) 
        (let [e (first @ck)]
          (if (nil? e)
            [:nokeyerr]
            (do 
              (swap! ck #(next %))
              [:ok e])))
        [:wrongtypeerr]))
    [:nokeyerr]))

(defn lrange-cmd
  "Get a range of elements from a list"
  [db k start end]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      (if (counted-list? @ck)
        [:ok (drop (fix-type start) (take (fix-type end) @ck))]
        [:wrongtypeerr]))
    [:emptymultibulk]))    

(defn rpush-cmd
  "Append one or multiple values to a list"
  [db k v]
  (if (not (exists? db k))
    (do
      (intern db (symbol k) (atom (counted-double-list (fix-type v))))
      [:cone])
    (let [ck (get-atom db k)]
      (if (counted-list? @ck) 
        (do 
          (swap! ck #(conj % (fix-type v)))
          [:int (count @ck)])
        [:wrongtypeerr]))))

(defn rpushx-cmd
  "Append one value to a list"
  [db k v]
  (if (exists? db k)
    (rpush-cmd db k v)
    [:czero]))

(defn rpop-cmd
  "Remove and get the first element in a list"
  [db k]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      (if (counted-list? @ck) 
        (let [e (peek @ck)]
          (if (nil? e)
            [:nokeyerr]
            (do 
              (swap! ck #(pop %))
              [:ok e])))
        [:wrongtypeerr]))
    [:nokeyerr]))

  

