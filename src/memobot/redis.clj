(ns memobot.redis)
(use '[clojure.string :only (join split)])

; Redis command to function mappings
; This is the meaning of the flags:
; w: write command (may modify the key space).
; r: read command  (will never modify the key space).

(def commands 
  {:use            'use-db
   :set            ['memobot.strings/set-cmd #{:any} "w" 0 :just-ok]
   :get            ['memobot.strings/get-cmd #{:string :long :integer} "r" :nokeyerr :ok]
   :keys           ['memobot.core/keys-cmd #{:any} "rs" :ok :ok]
   :type           ['memobot.core/type-cmd #{:any} "r" :cnone :ok] 
   :del            ['memobot.core/del-cmd #{:any} "w!" :czero :cone] 
   :ping           ['memobot.core/ping-cmd #{:any} "r" :pong :pong]
   :incr           ['memobot.strings/incr-cmd #{:long :integer} "w" 0 :int]
   :decr           ['memobot.strings/decr-cmd #{:long :integer} "w" 0 :int]
   :incrby         ['memobot.strings/incrby-cmd #{:long :integer} "w" 0 :int]
   :decrby         ['memobot.strings/decrby-cmd #{:long :integer} "w" 0 :int]
   :setnx          ['memobot.strings/setnx-cmd #{:any} "w" :new :int]
   :strlen         ['memobot.strings/strlen-cmd #{:string :long :integer} "r" :czero :int]
   :hdel           ['memobot.hashes/hdel-cmd #{:any} "w" :czero :cone]
   :hget           ['memobot.hashes/hget-cmd #{:any} "r" :nokeyerr :ok]
   :hgetall        ['memobot.hashes/hgetall-cmd #{:any} "r" :emptymultibulk :ok]
   :hexists        ['memobot.hashes/hexists-cmd #{:any} "r" :czero :int]
   :hkeys          ['memobot.hashes/hkeys-cmd #{:any} "r" :emptymultibulk :ok]
   :hlen           ['memobot.hashes/hlen-cmd #{:any} "r" :czero :int]
   :hset           ['memobot.hashes/hset-cmd #{:any} "w" {} :cone] 
   :hsetnx         ['memobot.hashes/hsetnx-cmd #{:any} "w" {} :cone]
   :hvals          ['memobot.hashes/hvals-cmd #{:any} "r" :emptymultibulk :ok]
   :lindex         ['memobot.lists/lindex-cmd "r" :nokeyerr]
   :llen           ['memobot.lists/llen-cmd "r" :czero]
   :lpop           ['memobot.lists/lpop-cmd "w" :nokeyerr]
   :lpush          ['memobot.lists/lpush-cmd "w" '()]
   :lpushx         ['memobot.lists/lpushx-cmd "w" nil]
   :lrange         ['memobot.lists/lrange-cmd "r" :emptymultibulk]
   :lrem           ['memobot.lists/lrem-cmd "w" "0"]
   :lset           ['memobot.lists/lset-cmd "w" :nosuchkey]
   :ltrim          ['memobot.lists/ltrim-cmd "w" '()]
   :rpop           ['memobot.lists/rpop-cmd "w" :nokeyerr] 
   :rpush          ['memobot.lists/rpush-cmd "w" '()]
   :rpushx         ['memobot.lists/rpushx-cmd "w" nil] 
   :sadd           ['memobot.sets/sadd-cmd "w" {}]
   :scard          ['memobot.sets/scard-cmd "r" :czero]
   :sdiff          ['memobot.sets/sdiff-cmd "r" :emptymultibulk]
   :sinter         ['memobot.sets/sinter-cmd "r" :emptymultibulk]
   :sismember      ['memobot.sets/sismember-cmd "r" :czero]
   :smembers       ['memobot.sets/smembers-cmd "r" :emptymultibulk]
   :smove          ['memobot.sets/smove-cmd "w" {}]
   :spop           ['memobot.sets/spop-cmd "w" {}]
   :srandmember    ['memobot.sets/srandmember-cmd "r" :nokeyerr]
   :srem           ['memobot.sets/srem-cmd "w" {}]
   :sunion         ['memobot.sets/sunion-cmd "r" :emptymultibulk]
   :zadd           ['memobot.sorted-sets/zadd-cmd "w" {}] 
   :zcard          ['memobot.sorted-sets/zcard-cmd  "r" :czero]
   :zcount         ['memobot.sorted-sets/zcount-cmd "r" :czero]
   :zincrby        ['memobot.sorted-sets/zincrby-cmd "w" {}]
   :zrange         ['memobot.sorted-sets/zrange-cmd "r" :emptymultibulk]
   :zrangebyscore  ['memobot.sorted-sets/zrangebyscore-cmd "r" :emptymultibulk]
   :zrank          ['memobot.sorted-sets/zrank-cmd "w" {}]
   :zrem           ['memobot.sorted-sets/zrem-cmd "w" {}]
   :zrevrange      ['memobot.sorted-sets/zrevrange-cmd "r" :emptymultibulk]
   :zscore         ['memobot.sorted-sets/zscore-cmd "r" :nokeyerr]
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
  :cnone "+none"
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

(def short-response #{:cone :czero :just-ok})

(defn format-reply
  [msg]
    (let [[t result] msg]
      (if (contains? short-response t)
        (str (reply-msg t) "\r\n")
        (if (coll? result)
          (to-redis-proto result)
          (str (reply-msg t) result "\r\n")))))
