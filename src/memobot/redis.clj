(ns memobot.redis)
(use '[clojure.string :only (join split)])

; Redis command to function mappings
; This is the meaning of the flags:
; w: write command (may modify the key space).
; r: read command  (will never modify the key space).

(def commands 
  {:use            'use-db
   :set            ['memobot.strings/set-cmd "w"]
   :get            ['memobot.strings/get-cmd "r"]
   :keys           ['memobot.core/keys-cmd "r"]
   :type           ['memobot.core/type-cmd "r"]
   :del            ['memobot.core/del-cmd "w"]
   :ping           ['memobot.core/ping-cmd "r"]
   :incr           ['memobot.strings/incr-cmd "w"]
   :decr           ['memobot.strings/decr-cmd "w"]
   :incrby         ['memobot.strings/incrby-cmd "w"]
   :decrby         ['memobot.strings/decrby-cmd "w"]
   :setnx          ['memobot.strings/setnx-cmd "w"]
   :strlen         ['memobot.strings/strlen-cmd "r"]
   :hdel           ['memobot.hashes/hdel-cmd "w"]
   :hget           ['memobot.hashes/hget-cmd "r"]
   :hgetall        ['memobot.hashes/hgetall-cmd "r"]
   :hexists        ['memobot.hashes/hexists-cmd "r"]
   :hkeys          ['memobot.hashes/hkeys-cmd "r"]
   :hlen           ['memobot.hashes/hlen-cmd "r"]
   :hset           ['memobot.hashes/hset-cmd "w"]
   :hsetnx         ['memobot.hashes/hsetnx-cmd "w"]
   :hvals          ['memobot.hashes/hvals-cmd "r"]
   :lindex         ['memobot.lists/lindex-cmd "r"]
   :llen           ['memobot.lists/llen-cmd "r"]
   :lpop           ['memobot.lists/lpop-cmd "w"]
   :lpush          ['memobot.lists/lpush-cmd "w"]
   :lpushx         ['memobot.lists/lpushx-cmd "w"]
   :lrange         ['memobot.lists/lrange-cmd "r"]
   :lrem           ['memobot.lists/lrem-cmd "w"]
   :lset           ['memobot.lists/lset-cmd "w"]
   :ltrim          ['memobot.lists/ltrim-cmd "w"]
   :rpop           ['memobot.lists/rpop-cmd "w"]
   :rpush          ['memobot.lists/rpush-cmd "w"]
   :rpushx         ['memobot.lists/rpushx-cmd "w"]
   :sadd           ['memobot.sets/sadd-cmd "w"]
   :scard          ['memobot.sets/scard-cmd "r"]
   :sdiff          ['memobot.sets/sdiff-cmd "r"]
   :sinter         ['memobot.sets/sinter-cmd "r"]
   :sismember      ['memobot.sets/sismember-cmd "r"]
   :smembers       ['memobot.sets/smembers-cmd "r"]
   :smove          ['memobot.sets/smove-cmd "w"]
   :spop           ['memobot.sets/spop-cmd "w"]
   :srandmember    ['memobot.sets/srandmember-cmd "r"]
   :srem           ['memobot.sets/srem-cmd "w"]
   :sunion         ['memobot.sets/sunion-cmd "r"]
   :zadd           ['memobot.sorted-sets/zadd-cmd "w"] 
   :zcard          ['memobot.sorted-sets/zcard-cmd  "r"]
   :zcount         ['memobot.sorted-sets/zcount-cmd "r"]
   :zincrby        ['memobot.sorted-sets/zincrby-cmd "w"]
   :zrange         ['memobot.sorted-sets/zrange-cmd "r"]
   :zrangebyscore  ['memobot.sorted-sets/zrangebyscore-cmd "r"]
   :zrank          ['memobot.sorted-sets/zrank-cmd "w"]
   :zrem           ['memobot.sorted-sets/zrem-cmd "w"]
   :zrevrange      ['memobot.sorted-sets/zrevrange-cmd "r"] 
   :zscore         ['memobot.sorted-sets/zscore-cmd "r"]
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
  :int ":"
  :czero ":0"
  :cone  ":1"
  :nokeyerr "$-1"
  :nosuchkey "-ERR no such key"
  :syntaxerr "-ERR syntax error"
  :wrongtypeerr "-ERR Operation against a key holding the wrong kind of value"
  :nointerr "-ERR value is not an integer or out of range"
  :outofrangeerr "-ERR index out of range"
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
