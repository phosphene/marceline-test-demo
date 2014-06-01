(ns marceline-test-demo.core-test
  (:import storm.trident.TridentTopology
           [storm.trident.operation.builtin MapGet]
           [storm.trident.testing MemoryMapState$Factory FixedBatchSpout]
           [backtype.storm LocalDRPC LocalCluster StormSubmitter])
  (:require [clojure.test :refer :all]
            [marceline-test-demo.core :refer :all]
            [backtype.storm.testing :as t])
  (:use clojure.test
        storm.trident.testing
        marceline-test-demo.core
        [backtype.storm config]))


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
              TOPOLOGY-STATS-SAMPLE-RATE 1.00}
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


(deftest wordcount-drpc
  (t/with-local-cluster [cluster]
    (with-drpc [drpc]
      (let [feeder (feeder-spout ["sentence"])
            topology (build-topology feeder drpc)]
        (with-topology [cluster topology]
          (feed feeder TEST-VALS)
          (is (= 4
                 (ffirst
                  (exec-drpc drpc
                             "words"
                             "cat dog the man jumped")))))))))



