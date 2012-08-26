(ns memobot.core)

(def db 'db1)

(defn use-db
  "Change a database (namespace)"
  [n]
  (create-ns (symbol n))
  (intern *ns* 'db (symbol n))  )

(defn set-key 
  "Sets the value of a key"
  [k v]
  (intern db (symbol k) (atom v)))

(defn get-key 
  "Get the value of a key"
  [k]
  (if (ns-resolve db (symbol k))
    (deref (eval (symbol (str db "/" k))))
    nil))

(defn show-keys
  "Find all keys matching the given pattern
  TODO: pattern matching"
  [pattern]
  (keys (ns-interns db)))

(defn key-type
  "Determine the type stored at key"
  [k]
  (if (get-key k) 
    (.replace (.toLowerCase (str (type (deref (get-key k))))) "class java.lang." "" )
    "none"))

(defn del 
  "Delete a key"
  [k]
  (ns-unmap db (symbol k)))


; default database
(use-db "db1")

; test 
; (exec "*3\r\n$3\r\nset\r\n$4\r\nsdsd\r\n$2\r\n24\r\n")
; (prn (exec "*2\r\n$3\r\nget\r\n$4\r\nsdsd\r\n"))

; (exec "*3\r\n$3\r\nset\r\n$4\r\nsome3\r\n$2\r\n666\r\n")
; (prn (exec "*2\r\n$3\r\nget\r\n$4\r\nsome3\r\n"))

