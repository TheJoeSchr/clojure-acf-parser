#!/usr/bin/env bb
;; UNFINISHED
(ns acf.compat-unused (:require
                       [babashka.fs :as fs]
                       [clojure.edn :as edn]
                       [clojure.java.io :as io]
                       [clojure.tools.cli :refer [parse-opts]]
                       [clojure.data :refer [diff]]
                       [clojure.string :as str]))

(load-file "src/acf/parser.clj")
(require '[acf.parser :as parser])

;;
(defn basename [path]
  (last (.split path fs/file-separator)))

(defn basepath [path]
  (str/join "/" (drop-last (.split path fs/file-separator))))

(comment (basepath *file*))
(comment (basename *file*))

(def cli-options
  ;; An option with a required argument
  [["-p" "--path DIR" "Path to directory with 'appmanifest_*' files to compare stdin list of files too"
    :multi true ; use :update-fn to combine multiple instance of -f/--file
    :default []
    ;; with :multi true, the :update-fn is passed both the existing parsed
    ;; value(s) and the new parsed value from each option
    :update-fn conj]
   ["-c" "--[no-]common" "Add common paths installdir" :default true]
   ["-n" "--pprint" "Pretty print games that are in both lists" :default false]
   ["-d" "--debug" "Debug prints" :default false]
   ["-h" "--help"]])
(defn vec->basename-set [path-vector] (set (map basename path-vector)))
(defn in-both [local remote] (nth (diff remote local) 2))

(defn diff-vec-of-paths [local-vec remote-vec]
  "gets 2 vectors, return filenames that are in boths"
  (let [local (vec->basename-set local-vec)
        remote (vec->basename-set remote-vec)]
    (in-both local remote)))

(defn find-filenames-in-filepaths [filepaths filenames]
  "compares list of filenames to see if it's included in list of filepaths "
  (reduce (fn [newseq filename]
            (cons (first (filter #(str/includes? % filename) filepaths)) newseq))
          []
          filenames))
(comment
  (do
    (def -local ["test/files/appmanifest_940710.acf" "test/files/appmanifest_1190460.acf" "test/files/appmanifest_1282730.acf" "test/files/appmanifest_1434950.acf" "test/files/appmanifest_1522820.acf"])
    (def -remote ["remote/compat/9999999" "remote/compat/1282730" "remote/compat/1434950" "remote/compat/1522820"])
    (def -newlocal (map #(-> (str/replace % ".acf" "")) (map #(-> (str/replace % "appmanifest_" "")) (vec->basename-set -local))))
    (def -newremote (map basename -remote))
    (def -in-both (diff-vec-of-paths -newlocal -newremote))
    (println (vec -in-both))
    ; works until here, but we need not in both
    ))
; above by chatgpt
(defn get-common-paths [local remote]
  (def local ["test/files/appmanifest_940710.acf" "test/files/appmanifest_1190460.acf" "test/files/appmanifest_1282730.acf" "test/files/appmanifest_1434950.acf" "test/files/appmanifest_1522820.acf"])
  (def remote ["remote/compat/9999999" "remote/compat/1282730" "remote/compat/1434950" "remote/compat/1522820"])
  (let [newlocal (map #(-> (str/replace % ".acf" "") (str/replace % "appmanifest_" "")) (map vec->basename-set (map basename local)))
        newremote (map basename remote)
        in-both (diff-vec-of-paths newlocal newremote)]
    (vec in-both)))
(comment
  (do
    (def -local ["test/files/appmanifest_940710.acf" "test/files/appmanifest_1190460.acf" "test/files/appmanifest_1282730.acf" "test/files/appmanifest_1434950.acf" "test/files/appmanifest_1522820.acf"])
    (def -remote ["remote/compat/9999999" "remote/compat/1282730" "remote/compat/1434950" "remote/compat/1522820"])
    (get-common-paths -local -remote)))
(defn surround-string [string surround-with]
  (str surround-with string surround-with))
(defn surround-with-doublequotes [string]
  (surround-string string "\""))

(comment
  (surround-string "tesx" "!")
  (surround-with-doublequotes "test"))

(defn -main [& args]
  (let [local (:arguments (parse-opts args cli-options))
        remote (vec (line-seq (io/reader *in*)))
        options (:options (parse-opts args cli-options))
        debug (:debug options)
        pprint (:pprint options)
        help (:help options)
        common (:common options)]
    (when help
      (println cli-options))
    (when debug
      (println "options:" options)
      (println "diff" remote)
      (println "filenames" local)
      (println "Results:\n"))

    (let [in-both (diff-vec-of-paths local remote)]
      (doseq [filename (find-filenames-in-filepaths local in-both)]
        (if pprint
          ;pretty print names
          (println (parser/get-key-from-acf :name filename))
          ;else
          (do
            (println filename)
            ; path of installdir

; (str (basepath *file*) fs/file-separator "common")

            (let [installdir (parser/get-key-from-acf :installdir filename)
                  commonpath (if common (str/join fs/file-separator [(basepath filename) "common"]) "")] (println (str/join "/" [commonpath (surround-with-doublequotes installdir)])))))))))

; helps running file directly via bb -f
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
