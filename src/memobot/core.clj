(ns memobot.core
  (:use [memobot types redis strings hashes lists sets sorted-sets]))

(defn keys-cmd
  "Find all keys matching the given pattern"
  [mask]
  (let [all-keys (keys (ns-interns 'db1)) 
        pattern  (re-pattern (.replace (name mask) "*" ".*"))]
    (filter #(re-matches pattern (str %)) all-keys)))

(defn type-cmd
  "Determine the type stored at key"
  [k]
  (get all-types (symbol (.replace (str (type k)) "class " "")) "none"))

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
            (= mode "w!")
              [response-type (apply (resolve command) k args)]
            (= mode "r")
              [response-type (apply (resolve command) (deref (eval k)) args)]
            (= mode "w")
              [response-type (swap! (eval k) #( apply (resolve command) % args ) )])
          [:wrongtypeerr])
    ))

(defn exec
  "Executes a command"
  [db redis-command]
   (let [k (get-key db (nth redis-command 1 nil))
         args (fix-types (drop 2 redis-command))
         command-table ((keyword (symbol (.toLowerCase (first redis-command)))) commands)
         mode (command-table 2)
         command (-> (command-table 0) str .toLowerCase symbol)
         empty-val (command-table 3)
         response-type (command-table 4)
         key-exists? (not (nil? (exists? k)))]
     (try
        (cond 

          ;write for non-existing key
          (and (not key-exists?) (or (= mode "w") (= mode "w!")))
            (if (contains? #{:nokeyerr :czero} empty-val)
              [empty-val]
              (do
                (if (not (nil? empty-val))
                  (init-atom k empty-val))
                  (run-func command-table k args)))

          ;read for non-existing key
          (and (not key-exists?) (= mode "r"))
            [empty-val]
          
          ;read state (no key provided) 
          (= mode "rs")
            [response-type (apply (resolve command) k args)]

          ;key exists
          (true? key-exists?)
            (run-func command-table k args))

     (catch clojure.lang.ArityException e [:just-err, (str " wrong number of arguments for '" command "' command")])
     (catch IndexOutOfBoundsException e [:outofrangeerr])
     (catch NullPointerException e [:just-err, (str " unknown command '" (first redis-command) "'")]))))
    
(defn process
  [db protocol-str]
  (format-reply 
    (exec db (list* (from-redis-proto protocol-str)))))
