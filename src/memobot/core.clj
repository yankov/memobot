(ns memobot.core
  (:use [memobot types redis strings hashes lists sets sorted-sets]))

; TODO: 
; handle wrong type, non-eget-atom,

(defn keys-cmd
  "Find all keys matching the given pattern"
  [db mask]
  (let [all-keys (keys (ns-interns db)) 
        pattern  (re-pattern (.replace mask "*" ".*"))]
    [:ok (filter #(re-matches pattern (str %)) all-keys)]))

(defn type-cmd
  "Determine the type stored at key"
  [db k]
  (if (get-cmd db k) 
    [:ok (.replace (.toLowerCase (str (type (get (get-cmd db k) 1)))) "class java.lang." "" )]
    [:ok "none"]))

(defn del-cmd
  "Delete a key"
  [db k]
  (if (ns-resolve db (symbol k))
    (do (ns-unmap db (symbol k))
     [:cone])
  [:czero]))

(defn ping-cmd
  [db]
  [:pong])


(defn init-atom
  [k v]
  (intern (symbol (namespace k)) (symbol (name k)) (atom v)))

(defn exec
  "Executes a command"
  [db protocol-str]
   (let [redis-command (list* (from-redis-proto protocol-str))
         k (get-key db (nth redis-command 1))
         args (apply list* (list ''db1 (rest redis-command)))
         command-table ((keyword (symbol (first redis-command))) commands)
         mode (command-table 1)
         command (command-table 0)
         empty-val (command-table 2)
         key-exists? (not (nil? (exists? k)))]
     (try
        (cond 
          (and (not key-exists?) (= mode "w"))
            (if (contains? #{:nokeyerr :czero} empty-val)
              [empty-val]
              (do
                (if (not (nil? empty-val))
                  (init-atom k empty-val))
                (eval (conj args (resolve command)))))
          (and (not key-exists?) (= mode "r"))
            [empty-val]
          (and key-exists? (= mode "r"))
            (eval (conj args (resolve command)))
          (and key-exists? (= mode "w"))
            (eval (conj args (resolve command))))
     (catch clojure.lang.ArityException e [:just-err, (str " wrong number of arguments for '" command "' command")])
     (catch NullPointerException e [:just-err, (str " unknown command '" (first redis-command) "'")]))))


