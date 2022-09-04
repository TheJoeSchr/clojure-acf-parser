#!/usr/bin/env bb
(ns acf.unused (:require [babashka.fs :as fs]
                         [clojure.edn :as edn]
                         [clojure.java.io :as io]))
(load-file "src/acf/parser.clj")
(comment (keys (ns-publics *ns*)))
(require '[acf.parser :as parser ])
(comment (keys (ns-publics 'acf.parser)))

(defn filterGames
  "looksup acf-files installdir inside commonfolder and filters accordingly to `shouldExists`"
  [filenames commonpath shouldExists]
  (map :path
       (filter
        (fn [{exists :exists}] (= exists shouldExists))
        (parser/resolve-installs filenames commonpath))))

(comment
  (do   
    (def -filenames '("test/files/appmanifest_1190460.acf" "test/files/appmanifest_12210.acf" "test/files/appmanifest_1282730.acf" "test/files/appmanifest_1434950.acf"))
      (resolve-installs -filenames "test/files/common/")
      (filterGames -filenames "test/files/common/" false)
      (doseq [item (filterGames -filenames "test/files/common/" false)]
               (println item))
      ) )

(defn -main [& args]
  (let [filenames (line-seq (io/reader *in*))
        commonpath (or (first args) "common")
        games-found (filterGames filenames commonpath true)
        games-not-found (filterGames filenames commonpath false)]
    (do
      ; adding second arg prints debug info
      (if (> (count args) 1)
        (do (println (str "common:  " commonpath))
            (println (str "# files: " (count filenames)))
            (println (str "# games found: " (count games-found)))
            (println (str "# games not found: " (count games-not-found)))))
      ; only print missing games if we at least found one
      (if (< (count games-not-found) (count filenames))
        (doseq [acf-file games-not-found]
          (println acf-file ))
        ))))

; helps running file directly via bb -f
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
