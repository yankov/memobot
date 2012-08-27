(ns memobot.db)

(def db 'db1)

(defn use-db
  "Change a database (namespace)"
  [n]
  (create-ns (symbol n)))
  ; (intern *ns* 'db (symbol n))  )
