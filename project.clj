(defproject marceline-test-demo "0.1.0-SNAPSHOT"
  :description "Marceline Demonstration"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :main ^:skip-aot marceline-test-demo.core
  :target-path "target/%s"
  :profiles {:dev
             {:dependencies [
                             [storm "0.9.0.1"]
                             [yieldbot/marceline "0.2.0-SNAPSHOT"]
                             [midje "1.6.0"]
                             [lein-midje "3.1.3"]]}

             :uberjar {:aot :all}})
