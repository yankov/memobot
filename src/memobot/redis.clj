(ns memobot.redis)
(use '[clojure.string :only (join split)])
(require ['clojure.data.finger-tree :as 'ftree])

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
   :del            ['memobot.types/del-cmd #{:any} "w!" :czero :cone] 
   :ping           ['memobot.core/ping-cmd #{:any} "r" :pong :pong]
   :incr           ['memobot.strings/incr-cmd #{:long :integer} "w" 0 :int]
   :decr           ['memobot.strings/decr-cmd #{:long :integer} "w" 0 :int]
   :incrby         ['memobot.strings/incrby-cmd #{:long :integer} "w" 0 :int]
   :decrby         ['memobot.strings/decrby-cmd #{:long :integer} "w" 0 :int]
   :setnx          ['memobot.strings/setnx-cmd #{:any} "w" :new :int]
   :strlen         ['memobot.strings/strlen-cmd #{:string :long :integer} "r" :czero :int]
   :hdel           ['memobot.hashes/hdel-cmd #{:hash} "w" :czero :cone]
   :hget           ['memobot.hashes/hget-cmd #{:hash} "r" :nokeyerr :ok]
   :hgetall        ['memobot.hashes/hgetall-cmd #{:hash} "r" :emptymultibulk :ok]
   :hexists        ['memobot.hashes/hexists-cmd #{:hash} "r" :czero :int]
   :hkeys          ['memobot.hashes/hkeys-cmd #{:hash} "r" :emptymultibulk :ok]
   :hlen           ['memobot.hashes/hlen-cmd #{:hash} "r" :czero :int]
   :hset           ['memobot.hashes/hset-cmd #{:hash} "w" {} :cone] 
   :hsetnx         ['memobot.hashes/hsetnx-cmd #{:hash} "w" {} :cone]
   :hvals          ['memobot.hashes/hvals-cmd #{:hash} "r" :emptymultibulk :ok]
   :lindex         ['memobot.lists/lindex-cmd #{:list} "r"  :nokeyerr :ok]
   :llen           ['memobot.lists/llen-cmd #{:list} "r" :czero :int]
   :lpop           ['memobot.lists/lpop-cmd #{:list} "w!" :nokeyerr :ok]
   :lpush          ['memobot.lists/lpush-cmd #{:list} "w" (ftree/counted-double-list) :just-ok] ; according to r.protocol should return size of the list
   :lpushx         ['memobot.lists/lpushx-cmd #{:any} "w" :new :just-ok]
   :lrange         ['memobot.lists/lrange-cmd #{:list} "r" :emptymultibulk :ok]
   :lrem           ['memobot.lists/lrem-cmd #{:list} "w" "0"]
   :lset           ['memobot.lists/lset-cmd #{:list} "w" :nosuchkey :just-ok]
   :ltrim          ['memobot.lists/ltrim-cmd #{:list} "w" (ftree/counted-double-list)]
   :rpop           ['memobot.lists/rpop-cmd #{:list} "w!" :nokeyerr :ok] 
   :rpush          ['memobot.lists/rpush-cmd #{:list} "w" (ftree/counted-double-list) :just-ok]
   :rpushx         ['memobot.lists/rpushx-cmd #{:any} "w" :new :just-ok] 
   :sadd           ['memobot.sets/sadd-cmd #{:set} "w!" #{} :int]
   :scard          ['memobot.sets/scard-cmd #{:set} "r" :czero :int]
   :sdiff          ['memobot.sets/sdiff-cmd #{:set} "r" :emptymultibulk :ok]
   :sinter         ['memobot.sets/sinter-cmd #{:set} "rs" :emptymultibulk :ok]
   :sismember      ['memobot.sets/sismember-cmd #{:set} "r" :czero :int]
   :smembers       ['memobot.sets/smembers-cmd #{:set} "r" :emptymultibulk :ok]
   :smove          ['memobot.sets/smove-cmd #{:set} "w" #{}]
   :spop           ['memobot.sets/spop-cmd #{:set} "w" #{}]
   :srandmember    ['memobot.sets/srandmember-cmd #{:set} "r" :nokeyerr]
   :srem           ['memobot.sets/srem-cmd #{:set} "w!" #{} :czero :int] 
   :sunion         ['memobot.sets/sunion-cmd #{:set} "rs" :emptymultibulk :ok]
   :zadd           ['memobot.sorted-sets/zadd-cmd #{:hash} "w" {} :cone] 
   :zcard          ['memobot.sorted-sets/zcard-cmd #{:hash}  "r" :czero :int]
   :zcount         ['memobot.sorted-sets/zcount-cmd #{:hash} "r" :czero]
   :zincrby        ['memobot.sorted-sets/zincrby-cmd #{:hash} "w" {} :just-ok] ; todo: should return :int
   :zrange         ['memobot.sorted-sets/zrange-cmd #{:hash} "r" :emptymultibulk :ok]
   :zrangebyscore  ['memobot.sorted-sets/zrangebyscore-cmd #{:hash} "r" :emptymultibulk]
   :zrank          ['memobot.sorted-sets/zrank-cmd #{:hash} "w" {}]
   :zrem           ['memobot.sorted-sets/zrem-cmd #{:hash} "w" {} :just-ok] ;todoL should return :int
   :zrevrange      ['memobot.sorted-sets/zrevrange-cmd #{:hash} "r" :emptymultibulk :ok]
   :zscore         ['memobot.sorted-sets/zscore-cmd #{:hash} "r" :nokeyerr :int]
  })

(defn from-redis-proto
  "Converts redis protocol to a list"
  [args]
  (let [a (split args #"\r\n")]
    (rest (take-nth 2 a))))

(defn to-redis-proto
  "Converts a collection to redis protocol"
  [s]
  (apply str "*" (count s) "\r\n"
    (map #( str "$" (count (str %)) "\r\n" % "\r\n" ) s)))

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
