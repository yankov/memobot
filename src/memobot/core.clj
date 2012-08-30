(ns memobot.core
  (:use [memobot redis strings])
  )

; TODO: 
; * show-keys should support patterns

(defn keys-cmd
  "Find all keys matching the given pattern
  TODO: pattern matching"
  [db pattern]
  [:ok (keys (ns-interns db))])

(defn type-cmd
  "Determine the type stored at key"
  [db k]
  (if (get-cmd db k) 
    [:ok (.replace (.toLowerCase (str (type (get (get-cmd db k) 1)))) "class java.lang." "" )]
    [:ok "none"]))

(defn del-cmd
  "Delete a key"
  [db k]
  (if (ns-resolve db (symbol k))
    (do (ns-unmap db (symbol k))
     [:cone])
  [:czero]))

(defn ping-cmd
  []
  [:pong])

(defn exec
  "Executes a command"
  [db redis-command]
   (def command (list* (from-redis-proto redis-command)))
   (eval (conj (apply list* (list ''db1 (rest command))) (resolve ((keyword (symbol (first command))) commands)))))

; next TODO: incr/decr