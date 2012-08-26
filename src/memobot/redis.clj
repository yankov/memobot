(ns memobot.redis)
(use '[clojure.string :only (join split)])

; redis command to function mappings
(def commands 
  {:use  'use-db
   :set  'set-key
   :get  'get-key
   :keys 'show-keys
   :type 'key-type
   :del  'del})

(defn from-redis-proto
  "Converts redis protocol to a list"
  [args]
  (let [a (split args #"\r\n")]
    (rest (take-nth 2 a))))
