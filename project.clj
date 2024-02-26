(defproject manage-migrate "0.1.0-SNAPSHOT"
  :description "Uma maneira facil de fazer migrações em clojure."
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.3.0"]
                 [org.apache.logging.log4j/log4j-api "2.23.0"]
                 [org.apache.logging.log4j/log4j-core "2.23.0"]]
  :main ^:skip-aot manage-migrate.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/log4j2-factory"]}})
