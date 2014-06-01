(ns marceline-test-demo.core
 (:import storm.trident.TridentTopology
           [storm.trident.operation.builtin MapGet]
           [storm.trident.testing MemoryMapState$Factory FixedBatchSpout]
           [backtype.storm LocalDRPC LocalCluster StormSubmitter])
  (:require [marceline.storm.trident :as t]
            [clojure.string :as string :only [split]])
  (:use [backtype.storm config])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
