#!/usr/bin/env bb
(ns acf.diff (:require 
                         [babashka.fs :as fs]
                         [clojure.edn :as edn]
                         [clojure.java.io :as io]
                         [clojure.tools.cli :refer [parse-opts]]
                         [clojure.data :refer [diff]]))

;;
(def cli-options
  ;; An option with a required argument
  [
   ["-p" "--path DIR" "Path to directory with 'appmanifest_*' files to compare stdin list of files too"
    :multi true ; use :update-fn to combine multiple instance of -f/--file
    :default []
    ;; with :multi true, the :update-fn is passed both the existing parsed
    ;; value(s) and the new parsed value from each option
    :update-fn conj]
   ["-d" "--debug" "Debug prints" :default false]
   ["-h" "--help"]])

(defn basename [path]
 (last (.split path fs/file-separator)) )
(defn vec->basename-set [path-vector](set (map basename path-vector)))
(comment 
  (let [local ["test/files/appmanifest_940710.acf" "test/files/appmanifest_1190460.acf" "test/files/appmanifest_1282730.acf" "test/files/appmanifest_1434950.acf" "test/files/appmanifest_1522820.acf"] 
        remote ["test/files/appmanifest_9999999.acf" "test/files/appmanifest_1282730.acf" "test/files/appmanifest_1434950.acf" "test/files/appmanifest_1522820.acf"]]
      (diff-vec-of-paths local remote)
      )
)


(defn diff-vec-of-paths [local-vec remote-vec] 
  "gets 2 vectors, return filenames that are in boths"
  (let [local (vec->basename-set local-vec) 
        remote (vec->basename-set remote-vec)]
    (let [in-both (nth (diff remote local) 2)] 
      in-both)
    )
)

(defn -main [& args]
  (let [local (:arguments (parse-opts args cli-options))
        remote (vec(line-seq (io/reader *in*)))
        options (:options (parse-opts args cli-options))]
    (do
      (when (:debug options)  
        (println "options:" options)
        (println "diff" remote)
        (println "filenames" local)
        )
      )
    (let [in-both (diff-vec-of-paths local remote)]
      (doseq [filename in-both]
        (println filename)
        )
      )
    )
  )
    




; helps running file directly via bb -f
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
