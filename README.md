memobot
=======

Memobot is a data structure server written in clojure. It speaks Redis protocol,
so any standard redis client can work with it.

Supported Features
==================  
* **Basic**: `keys`, `type`, `del`, `ping`  
* **Strings**: `set`, `get`, `incr`, `decr`, `incrby`, `decrby`, `setnx`, `strlen`  
* **Lists**: `lindex`, `llen`, `lpop`, `lpush`, `lpushx`, `lrange`, `lrem`, `lset`, `ltrim`, `rpop`, `rpush`, `rpushx`  
* **Hashes**: `hdel`, `hget`, `hgetall`, `hexists`, `hkeys`, `hlen`, `hset`, `hsetnx`, `hvals`  
* **Sets**: `sadd`, `scard`, `sdiff`, `sinter`, `sismember`, `smembers`, `spop`, `srem`, `sunion`  
* **Sorted Sets**: `zadd`, `zcard`, `zincrby`, `zrange`, `zrem`, `zscore`  

Details on how to use these commands can be found [here](http://redis.io/commands)

TODO
====

* Refactor sorted sets. Should probably use skip list or finger trees to be able to search in log(n) time.  
* Fix bugs that are breaking redis-benchmark.  
* Clustering