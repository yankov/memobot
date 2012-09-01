(ns memobot.lists
  (:use [memobot types]))

;TODO:
; lpop
; lpushx
; lrange
; lrem
; lset
; ltrim
; rpop
; rpush
; rpushx

(defn lindex-cmd
  "Get an element from a list by its index"
  [db k i]
  (if (exists? db k)
    (let [ck (get-atom db k)]
      [:ok (nth @ck (fix-type i) nil)])
    [:nokeyerr]))

; TODO: count is probably expensive on list?
; figure how to get an index of the last element
(defn lpush-cmd
  "Prepend one or multiple values to a list"
  [db k v]
  (if (not (exists? db k))
    (do
      (intern db (symbol k) (atom (list (fix-type v))))
      [:cone])
    (let [ck (get-atom db k)]
      (if (list? @ck) 
        (do 
          (swap! ck #(conj % (fix-type v)))
          [:int (count @ck)])
        [:wrongtypeerr]))))

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
      (if (list? @ck) 
        (let [e (first @ck)]
          (if (nil? e)
            [:nokeyerr]
            (do 
              (swap! ck #(pop %))
              [:ok e])))
        [:wrongtypeerr]))
    [:nokeyerr]))

    
  

