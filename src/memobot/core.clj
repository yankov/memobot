(ns memobot.core
  (:use [memobot types redis strings hashes lists sets sorted-sets]))

(defn keys-cmd
  "Find all keys matching the given pattern"
  [mask]
  (let [all-keys (keys (ns-interns 'db1)) 
        pattern  (re-pattern (.replace mask "*" ".*"))]
    (filter #(re-matches pattern (str %)) all-keys)))

(defn type-cmd
  "Determine the type stored at key"
  [k]
  (.replace (.toLowerCase (str (type k))) "class java.lang." "" ))

(defn del-cmd
  "Delete a key"
  [k]
  (ns-unmap (symbol (namespace k)) (symbol (name k))))

(defn ping-cmd
  []
  true)

(defn init-atom
  [k v]
  (intern (symbol (namespace k)) (symbol (name k)) (atom v)))

(defn run-func
  [command-table k args]

  (let [command (first command-table)
        response-type (command-table 4)
        allowed-types (command-table 1)
        mode (command-table 2)]
        (if (or (= allowed-types #{:any}) (contains? allowed-types (keyword (type-cmd (deref (eval k))))))
          (cond
            (= (name command) "del-cmd" )
              (do
                (del-cmd k)
                [:cone])
            (= mode "r")
              [response-type (apply (resolve command) (deref (eval k)) args)]
            (= mode "w")
              [response-type (swap! (eval k) #( apply (resolve command) % args ) )])
          [:wrongtypeerr])
    ))

(defn exec
  "Executes a command"
  [db protocol-str]
   (let [redis-command (list* (from-redis-proto protocol-str))
         k (get-key db (nth redis-command 1 nil))
         args (drop 2 redis-command)
         command-table ((keyword (symbol (first redis-command))) commands)
         mode (command-table 2)
         command (command-table 0)
         empty-val (command-table 3)
         key-exists? (not (nil? (exists? k)))]
     (try
        (cond 
          (= (name command) "set-cmd")
            (do 
              (apply set-cmd k args)
              [:just-ok])
          (= (name command) "keys-cmd" )
              [:ok (keys-cmd (name k))]
          (and (not key-exists?) (= mode "w"))
            (if (contains? #{:nokeyerr :czero} empty-val)
              [empty-val]
              (do
                (if (not (nil? empty-val))
                  (init-atom k empty-val))
                  (run-func command-table k args)))
          (and (not key-exists?) (= mode "r"))
            [empty-val]
          (true? key-exists?)
            (run-func command-table k args))
     (catch clojure.lang.ArityException e [:just-err, (str " wrong number of arguments for '" command "' command")])
     (catch NullPointerException e [:just-err, (str " unknown command '" (first redis-command) "'")]))))


