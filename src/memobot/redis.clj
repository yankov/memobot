(ns memobot.redis)
(use '[clojure.string :only (join split)])

; redis command to function mappings
(def commands 
  {:use            'use-db
   :set            'memobot.strings/set-cmd
   :get            'memobot.strings/get-cmd
   :keys           'memobot.core/keys-cmd
   :type           'memobot.core/type-cmd
   :del            'memobot.core/del-cmd
   :ping           'memobot.core/ping-cmd
   :incr           'memobot.strings/incr-cmd
   :decr           'memobot.strings/decr-cmd
   :incrby         'memobot.strings/incrby-cmd
   :decrby         'memobot.strings/decrby-cmd
   :setnx          'memobot.strings/setnx-cmd
   :strlen         'memobot.strings/strlen-cmd
   :hdel           'memobot.hashes/hdel-cmd
   :hget           'memobot.hashes/hget-cmd
   :hgetall        'memobot.hashes/hgetall-cmd
   :hexists        'memobot.hashes/hexists-cmd
   :hkeys          'memobot.hashes/hkeys-cmd
   :hlen           'memobot.hashes/hlen-cmd
   :hset           'memobot.hashes/hset-cmd
   :hsetnx         'memobot.hashes/hsetnx-cmd
   :hvals          'memobot.hashes/hvals-cmd
   :lindex         'memobot.lists/lindex-cmd
   :llen           'memobot.lists/llen-cmd
   :lpop           'memobot.lists/lpop-cmd
   :lpush          'memobot.lists/lpush-cmd
   :lpushx         'memobot.lists/lpushx-cmd
   :lrange         'memobot.lists/lrange-cmd
   :lrem           'memobot.lists/lrem-cmd
   :lset           'memobot.lists/lset-cmd
   :ltrim          'memobot.lists/ltrim-cmd
   :rpop           'memobot.lists/rpop-cmd
   :rpush          'memobot.lists/rpush-cmd
   :rpushx         'memobot.lists/rpushx-cmd
   :sadd           'memobot.sets/sadd-cmd
   :scard          'memobot.sets/scard-cmd
   :sdiff          'memobot.sets/sdiff-cmd
   :sinter         'memobot.sets/sinter-cmd
   :sismember      'memobot.sets/sismember-cmd
   :smembers       'memobot.sets/smembers-cmd
   :smove          'memobot.sets/smove-cmd
   :spop           'memobot.sets/spop-cmd
   :srandmember    'memobot.sets/srandmember-cmd
   :srem           'memobot.sets/srem-cmd
   :sunion         'memobot.sets/sunion-cmd 
   :zadd           'memobot.sorted-sets/zadd-cmd 
   :zcard          'memobot.sorted-sets/zcard-cmd 
   :zcount         'memobot.sorted-sets/zcount-cmd 
   :zincrby        'memobot.sorted-sets/zincrby-cmd 
   :zrange         'memobot.sorted-sets/zrange-cmd 
   :zrangebyscore  'memobot.sorted-sets/zrangebyscore-cmd 
   :zrank          'memobot.sorted-sets/zrank-cmd 
   :zrem           'memobot.sorted-sets/zrem-cmd 
   :zrevrange      'memobot.sorted-sets/zrevrange-cmd 
   :zscore         'memobot.sorted-sets/zscore-cmd 
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
