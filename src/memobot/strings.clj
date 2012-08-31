(ns memobot.strings)

(defn fix-type [v]
  (try 
    (Integer/parseInt v)
  (catch NumberFormatException e v)))

(defn set-cmd 
  "Sets the value of a key"
  [db k v]
  (intern db (symbol k) (atom (fix-type v)))
  [:just-ok])

(defn setnx-cmd 
  "Set the value of a key, only if the key does not exist"
  [db k v]
  (if (not (resolve (symbol (str db "/" k))))
    (do 
       (set-cmd db k v)
       [:cone])
    [:czero]))

(defn get-cmd
  "Get the value of a key"
  [db k]
  (if (ns-resolve db (symbol k))
    [:ok (deref (eval (symbol (str db "/" k))))]
    [:nokeyerr]))

(defn strlen-cmd
  [db k]
  "Get the length of the value stored in a key"
  [:int (.length (str (get (get-cmd db k) 1)))])

(defn incr-cmd
  "Increment the integer value of a key by one"
  [db k]
  (let [ck (symbol (str db "/" k))]
    (try 
      (if (not (resolve ck))
        (set-cmd db k "0"))
      [:int (swap! (eval ck) inc)]
      (catch ClassCastException e (do (prn "caught exception: " (.getMessage e)) [:nointerr])))))

(defn decr-cmd
  "Decrement the integer value of a key by one"
  [db k]
  (let [ck (symbol (str db "/" k))]
    (try 
      (if (not (resolve ck))
        (set-cmd db k "0"))
      [:int (swap! (eval ck) dec)]
      (catch ClassCastException e (do (prn "caught exception: " (.getMessage e)) [:nointerr])))))

(defn incrby-cmd
  "Increment the integer value of a key by the given amount"
  [db k v]
  (let [ck (symbol (str db "/" k))]
    (try 
      (if (not (resolve ck))
        (set-cmd db k "0"))
      [:int (swap! (eval ck) #(+ % (fix-type v)))]
      (catch ClassCastException e (do (prn "caught exception: " (.getMessage e)) [:nointerr])))))

(defn decrby-cmd
  "Decrement the integer value of a key by the given amount"
  [db k v]
  (let [ck (symbol (str db "/" k))]
    (try 
      (if (not (resolve ck))
        (set-cmd db k "0"))
      [:int (swap! (eval ck) #(- % (fix-type v)))]
      (catch ClassCastException e (do (prn "caught exception: " (.getMessage e)) [:nointerr])))))


