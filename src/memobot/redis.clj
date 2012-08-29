(ns memobot.redis)
(use '[clojure.string :only (join split)])

; redis command to function mappings
(def commands 
  {:use  'use-db
   :set  'memobot.strings/set-key
   :get  'memobot.strings/get-key
   :keys 'memobot.core/show-keys
   :type 'memobot.core/key-type
   :del  'memobot.core/del})

(defn from-redis-proto
  "Converts redis protocol to a list"
  [args]
  (let [a (split args #"\r\n")]
    (rest (take-nth 2 a))))

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
  :pong "+PONG"
  })

(defn format-reply
  [msg]
    (let [[t result] msg]
      (str (reply-msg t) result "\r\n")))