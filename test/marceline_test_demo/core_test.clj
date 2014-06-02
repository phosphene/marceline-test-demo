(ns marceline-test-demo.core-test
  (:import storm.trident.TridentTopology
           [storm.trident.operation.builtin MapGet]
           [storm.trident.testing MemoryMapState$Factory FixedBatchSpout]
           [backtype.storm LocalDRPC LocalCluster StormSubmitter]
           [org.apache.log4j Logger])
  (:require [clojure.test :refer :all]
            [marceline-test-demo.core :refer :all]
            [backtype.storm.testing :as t]
            )

  (:use midje.sweet
        storm.trident.testing
        marceline-test-demo.core
        [backtype.storm config]
        [clojure.tools.logging]
        ))


(defn set-log-level [level]
  (.. (Logger/getLogger 
       "org.apache.zookeeper.server.NIOServerCnxn")
      (setLevel level))
  (.. (impl-get-log "") getLogger getParent
      (setLevel level)))

(defmacro with-quiet-logs [& body]
  `(let [ old-level# (.. (impl-get-log "") getLogger 
                         getParent getLevel) ]
     (set-log-level org.apache.log4j.Level/OFF)
     (let [ ret# (do ~@body) ]
       (set-log-level old-level#)
       ret#)))


(defn run-local! []
  (let [cluster (LocalCluster.)
        local-drpc (LocalDRPC.)
        spout (doto (mk-fixed-batch-spout 3)
                (.setCycle true))]
    (.submitTopology cluster "wordcounter"
                     {}
                     (.build
                      (build-topology
                       spout
                       local-drpc)))
    (Thread/sleep 10000)
    (.execute local-drpc "words" "cat dog the man jumped")
    (.shutdown cluster)
    (System/exit 0)))

(defn submit-topology! [env]
  (let [name "wordcounter"
        conf {TOPOLOGY-WORKERS 6
              TOPOLOGY-MAX-SPOUT-PENDING 20
              TOPOLOGY-MESSAGE-TIMEOUT-SECS 60
              TOPOLOGY-STATS-SAMPLE-RATE 1.00
              TOPOLOGY-DEBUG false}
        spout (doto (mk-fixed-batch-spout 3)
                (.setCycle true))]
    (StormSubmitter/submitTopology
     name
     conf
     (.build (build-topology
              spout
              nil)))))

(defn -main
  ([]
     (run-local!))
  ([env & args]
     (submit-topology! (keyword env))))


(def TEST-VALS [["the cow jumped over the moon"]
                ["four score and seven years ago"]
                ["how many can you eat"]
                ["to be or not to be the person"]])


(facts "wordcount examples"
       (t/with-local-cluster [cluster]
         (with-drpc [drpc]
           (let [feeder (feeder-spout ["sentence"])
                 topology (build-topology feeder drpc)]
             (with-topology [cluster topology]
               (feed feeder TEST-VALS)
               (fact "wordcount works"
                     (ffirst
                      (exec-drpc drpc
                                 "words"
                                 "cat dog the man jumped")) => 6 ))))))

