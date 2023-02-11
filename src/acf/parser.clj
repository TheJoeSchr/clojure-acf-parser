#!/usr/bin/env bb
(ns acf.parser (:require [babashka.fs :as fs]
                         [clojure.edn :as edn]
                         [clojure.java.io :as io]))

(def multiple-lines-path "test/multiline.acf")
(def full-acf-path "test/example.acf")

(comment
  (fs/exists? full-acf-path))

(defn split-by-newline [s]
  (clojure.string/split
   s #"\n"))

(defn read-by-line [filename]
  (split-by-newline
   (slurp filename)))

(defn read-file [filename]
  (when (fs/exists? filename)
    (read-by-line filename)))

(defn split-by-whitespace [s]
  (clojure.string/split s #"\t+"))

(defn trim-key-value [s]
  (clojure.string/trim s))

(comment (trim-key-value "\t asdf \t"))
(defn remove-quotes [s]
  (clojure.string/replace
   s #"\\|\"" {"\\" "" "\"" ""}))

(defn readkeyvalue [s]
  (split-by-whitespace
   (remove-quotes
    (trim-key-value s))))

(defn clean-lines [lines]
  (map
   #(readkeyvalue %) lines))


(defn keywordize-things [[key value & other]]
  (hash-map
   (keyword key) value))

(defn mapify-lines [lines]
  (map #(keywordize-things %)

       lines))

(comment (keywordize-things
          ["appid" "1059980"]))


(comment (first (clean-lines multiple-lines-file)))
(comment
  (keywordize-things (first (clean-lines 
multiple-lines-file))))
(comment (mapify-lines
          (clean-lines
           multiple-lines-file)))

(defn parse-by-line [lines]
  (reduce into
          (mapify-lines (clean-lines
                         lines))))

(def multiple-lines-file (read-file multiple-lines-path))
(def acf-file (read-file full-acf-path))
(comment (parse-by-line multiple-lines-file))
(comment (:installdir (parse-by-line multiple-lines-file)))

; (map (fn [unmapped-row]
;        (map vector vamp-keys unmapped-row))
;      rows)
(comment (parse-by-line acf-file))
(comment
  (:installdir
   (parse-by-line
    acf-file)))

(defn parse-file [file]
  (parse-by-line
   (read-file file)))


(defn get-key-from-acf [keyname file]
  (keyname (parse-file file)))

(defn resolve-installs
  [filenames commonpath]
  (reduce
   (fn [files file]
    (let [installdir (get-key-from-acf :installdir file)
          exists (fs/exists? (str commonpath installdir))]
           (into files [{:path file :installdir installdir :exists exists}])))
             ()
             filenames))

(comment 
  (let [-filenames '("test/files/appmanifest_1190460.acf" "test/files/appmanifest_12210.acf" "test/files/appmanifest_1282730.acf" "test/files/appmanifest_1434950.acf")]
    (comment (do 
               (defn mapInstalldir [filenames] (map #(:installdir (parse-file %)) filenames))
               (mapInstalldir -filenames) ))
    (resolve-installs -filenames "common") ))
