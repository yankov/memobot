(use '[clojure.string :only (join split)])

; redis command to function mappings
(def commands {
               :use  use-db
               :set  set-key
               :get  get-key
               :keys show-keys
               :type key-type
               :del  del})

(defn from-redis-proto
  "Converts redis protocol to a list"
  [args]
  (let [a (split args #"\r\n")]
    (rest (take-nth 2 a))))


(defn exec
  "Executes a command"
  [redis-command]
   (def command (list* (from-redis-proto redis-command)))
   (eval (conj (apply list (rest command)) ((keyword (first command)) commands))))
