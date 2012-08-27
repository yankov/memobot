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
