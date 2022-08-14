(ns acf.parser)

(def multiple-lines-path "test/multiline.acf")

(defn read-file [filename]
  (slurp filename))
(defn split-by-newline [s]
  (clojure.string/split s #"\n"))

(defn read-by-line [filename]
  (split-by-newline
   (read-file filename)))

(def multiple-lines-file (read-by-line multiple-lines-path))
(comment multiple-lines-file)

(defn split-by-whitespace [s]
  (clojure.string/split s #"\t+"))

(defn trim-key-value [s]
  
(clojure.string/trim
 s))

(defn remove-quotes [s]
  (clojure.string/replace
   s #"\\|\""
   {"\\" "" "\"" ""}))

(defn readkeyvalue [s]
  (split-by-whitespace
   (remove-quotes
    (trim-key-value s))))

(defn keywordize-things [[key value & other]]
  (hash-map
   (keyword key) value))

(comment (read-by-line
          multiple-lines-file))

(map (fn [unmapped-row]
       (map vector vamp-keys unmapped-row))
     rows)

(defn clean-lines [lines]
  (reduce into
          (map #(readkeyvalue %)
               lines)))

(comment (clean-lines
          multiple-lines-file))

(defn parse-by-line [multiple-lines]
  (reduce into
          (map #(keywordize-things (readkeyvalue %))
               multiple-lines)))

(comment (:installdir (parse-by-line
                       multiple-lines-file)))
(defn -main [& args]
  (:installdir (parse-by-line multiple-lines-file)))
