(defproject memobot "1.0.0-SNAPSHOT"
  :description "Simple data structure server"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.jboss.netty/netty "3.2.7.Final"]
                 [org.clojure/data.finger-tree "0.0.1"]]
  :main memobot.server
  :jvm-opts ["-server" "-Xms2048m" "-Xmx2048m" "-XX:+UseParallelGC" 
          "-XX:+AggressiveOpts" "-XX:+UseFastAccessorMethods"] )
