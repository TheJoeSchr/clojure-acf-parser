(def filename "./example.acf")
(defn read-file [filename]
  (slurp filename))

(defn read-by-line [filename]
  (split-by-newline (read-file filename)))


(defn split-by-newline [s]
  (clojure.string/split s #"\n"))

(defn split-by-whitespace [s]
  (clojure.string/split s #"\t+"))

(defn trim-key-value [s]
  (clojure.string/trim s))

(defn remove-quotes [s]
  (clojure.string/replace s #"\\|\"" {"\\" "" "\"" ""}))

(defn readkeyvalue [s]
  (split-by-whitespace
   (
remove-quotes
 (trim-key-value 
s))))

(defn keywordize-things [[key value & other]]
  (hash-map
   (keyword key) value))

(comment (read-by-line filename))
(defn parse-by-line [multiple-lines]
  (reduce
 into (map #(keywordize-things (readkeyvalue %)) multiple-lines)))

(comment (:installdir (parse-by-line (read-by-line filename))))
