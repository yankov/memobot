(ns memobot.types)

(defn del-cmd
  "Delete a key"
  [k]
  (ns-unmap (symbol (namespace k)) (symbol (name k))))

(defn fix-type [v]
  (try 
    (Integer/parseInt v)
  (catch NumberFormatException e v)))

(defn fix-types [v]
  (map #(fix-type %) (list* v)))

(defn exists? 
  ([k] (resolve k))
  ([db k] (ns-resolve db (symbol k))))

(defn get-key
  [db k]
  (symbol (str db "/" k)))

(defn get-atom
  ([k] (deref (eval k)))
  ([db k] (eval (get-key db k))))

(def all-types
  { 'java.lang.String "string"
    'java.lang.Integer "integer"
    'java.lang.Long "integer"
    'clojure.lang.PersistentArrayMap "hash"
    'clojure.lang.PersistentList "list"
    'clojure.data.finger_tree.CountedDoubleList "list"
    'clojure.lang.PersistentHashSet "set"
  })