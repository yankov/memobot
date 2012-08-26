(ns memobot.core
  (:use [memobot strings redis])
  )

(def db 'db1)

(defn use-db
  "Change a database (namespace)"
  [n]
  (create-ns (symbol n))
  (intern *ns* 'db (symbol n))  )

(defn show-keys
  "Find all keys matching the given pattern
  TODO: pattern matching"
  [db pattern]
  (keys (ns-interns db)))

(defn key-type
  "Determine the type stored at key"
  [db k]
  (if (get-key db k) 
    (.replace (.toLowerCase (str (type (get-key db k)))) "class java.lang." "" )
    "none"))

(defn del 
  "Delete a key"
  [db k]
  (ns-unmap db (symbol k)))


; default database
(use-db "db1")

(defn exec
  "Executes a command"
  [redis-command]
   (def command (list* (from-redis-proto redis-command)))
   (eval (conj (apply list* ( list 'db (rest command))) (resolve ((keyword (symbol (first command))) commands)))))


; test 
(exec "*3\r\n$3\r\nset\r\n$4\r\nsdsd\r\n$2\r\n24\r\n")
(prn (exec "*2\r\n$3\r\nget\r\n$4\r\nsdsd\r\n"))

(exec "*3\r\n$3\r\nset\r\n$4\r\nsome3\r\n$2\r\n666\r\n")
(prn (exec "*2\r\n$3\r\nget\r\n$4\r\nsome3\r\n"))

