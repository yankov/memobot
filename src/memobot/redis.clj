(ns memobot.redis)
(use '[clojure.string :only (join split)])

; redis command to function mappings
(def commands 
  {:use    'use-db
   :set    'memobot.strings/set-cmd
   :get    'memobot.strings/get-cmd
   :keys   'memobot.core/keys-cmd
   :type   'memobot.core/type-cmd
   :del    'memobot.core/del-cmd
   :ping   'memobot.core/ping-cmd
   :incr   'memobot.strings/incr-cmd
   :decr   'memobot.strings/decr-cmd
   :incrby 'memobot.strings/incrby-cmd
   :decrby 'memobot.strings/incrby-cmd
   :setnx  'memobot.strings/setnx-cmd
   :strlen 'memobot.strings/strlen-cmd
  })

(defn from-redis-proto
  "Converts redis protocol to a list"
  [args]
  (let [a (split args #"\r\n")]
    (rest (take-nth 2 a))))

(defn to-redis-proto
  "Converts a collection to redis protocol"
  [s]
  (apply str (str
    "*" (count s) "\r\n") 
    (map #( str "$" (count (.toString %)) "\r\n" % "\r\n" ) s)))

(def reply-msg {
  :just-ok "+OK"
  :just-err "-ERR"
  :ok "+"
  :err "-"
  :czero ":0"
  :cone  ":1"
  :nokeyerr "$-1"
  :syntaxerr "-ERR syntax error"
  :wrongtypeerr "-ERR Operation against a key holding the wrong kind of value"
  :nointerr "-ERR value is not an integer or out of range"
  :cnegone ":-1\r\n"
  :nullbulk "$-1\r\n"
  :nullmultibulk "*-1\r\n"
  :emptymultibulk "*0\r\n"
  :pong "+PONG"
  })

(defn format-reply
  [msg]
    (let [[t result] msg]
      (if (coll? result)
        (to-redis-proto result)
        (str (reply-msg t) result "\r\n"))))
