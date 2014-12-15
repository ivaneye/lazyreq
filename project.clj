(defproject lazyreq "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [clj-http "1.0.0"]
                 [im.chit/cronj "1.4.1"]
                 [korma "0.3.0"]
                 [mysql/mysql-connector-java "5.1.33"]
                 [com.taoensso/timbre "3.3.1"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:init     lazyreq.core.core/recall
          :handler lazyreq.core.core/app}
  :profiles
  {:uberjar {:aot :all}
   :production
    {:ring
      {:init lazyreq.core.core/recall
        :open-browser? false, :stacktraces? false, :auto-reload? false}},
    :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
