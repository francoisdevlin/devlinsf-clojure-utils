(ns lib.devlinsf.str-utils)

;;; String Merging & Slicing


(defmulti re-partition (fn[input-string & remaining-inputs] (class (first remaining-inputs))))

;  "Splits the string into a lazy sequence of substrings, alternating
;  between substrings that match the patthern and the substrings
;  between the matches.  The sequence always starts with the substring
;  before the first match, or an empty string if the beginning of the
;  string matches.
;
;  For example: (re-partition \"abc123def\" #\"[a-z]+\")
;
;  Returns: (\"\" \"abc\" \"123\" \"def\")"
(defmethod re-partition java.util.regex.Pattern
  [#^String string #^java.util.regex.Pattern re]
  (let [m (re-matcher re string)]
    ((fn step [prevend]
       (lazy-seq
        (if (.find m)
          (cons (.subSequence string prevend (.start m))
                (cons (re-groups m)
                      (step (+ (.start m) (count (.group m))))))
          (when (< prevend (.length string))
            (list (.subSequence string prevend (.length string)))))))
     0)))

(defmethod re-partition clojure.lang.PersistentList
  [#^String input-string patterns]
  (let [reversed (reverse patterns)
	pattern (first reversed)
	remaining (rest reversed)]
    (if (empty? remaining)
      (re-partition input-string pattern)
      (map #(re-partition % pattern) (re-partition input-string (reverse remaining))))))

(defmulti re-split (fn[input-string & remaining-inputs] (class (first remaining-inputs))))

;This methods does the actual work of the re-split method.  It is lazy.
(defmethod re-split java.util.regex.Pattern
  [#^String input-string #^java.util.regex.Pattern pattern]
  ((fn step[input-sequence]
     (lazy-seq
       (if (first input-sequence)
	 (cons (first input-sequence) (step (drop 2 input-sequence)))
	 '())))
   (re-partition input-string pattern)))

(defmethod re-split clojure.lang.PersistentList
  [#^String input-string patterns]
  (let [reversed (reverse patterns)
	pattern (first reversed)
	remaining (rest reversed)]
    (if (empty? remaining)
      (re-split input-string pattern)
      (map #(re-split % pattern) (re-split input-string (reverse remaining))))))

(defmethod re-split clojure.lang.PersistentArrayMap
  [#^String input-string map-options]
  (cond 
   (:marshal-fn map-options) (map (:marshal-fn map-options) (re-split input-string (dissoc map-options :marshal-fn)))
   (:length map-options) (take (:length map-options) (re-split input-string (dissoc map-options :length)))
   (:offset map-options) (drop (:offset map-options) (re-split input-string (dissoc map-options :offset)))
   'true (re-split input-string (:pattern map-options))))



(defmulti re-gsub (fn[input-string & remaining-inputs] (class (first remaining-inputs))))

;  "Replaces all instances of 'pattern' in 'string' with
;  'replacement'.  Like Ruby's 'String#gsub'.
;  
;  If (ifn? replacment) is true, the replacement is called with the
;  match.
;  "
(defmethod re-gsub java.util.regex.Pattern
  [#^String string #^java.util.regex.Pattern regex #^String replacement]
  (if (ifn? replacement)
    (let [parts (vec (re-partition regex string))]
      (apply str
             (reduce (fn [parts match-idx]
                       (update-in parts [match-idx] replacement))
                     parts (range 1 (count parts) 2))))
    (.. regex (matcher string) (replaceAll replacement))))

(defmethod re-gsub clojure.lang.PersistentList
  [#^String input-string regex-pattern-pairs]
  (let [reversed (reverse regex-pattern-pairs)
	pair (first reversed)
	remaining (rest reversed)]
    (if (empty? remaining)
      (re-gsub input-string (first pair) (second pair))    
      (re-gsub (re-gsub input-string (reverse remaining)) (first pair) (second pair)))))


(defmulti re-sub (fn[input-string & remaining-inputs] (class (first remaining-inputs))))

;  "Replaces the first instance of 'pattern' in 'string' with
;  'replacement'.  Like Ruby's 'String#sub'.
;  
;  If (ifn? replacement) is true, the replacement is called with
;  the match.
;  "
(defmethod re-sub java.util.regex.Pattern
  [#^String string #^java.util.regex.Pattern regex #^String replacement ]
  (if (ifn? replacement)
    (let [m (re-matcher regex string)]
      (if (.find m)
        (str (.subSequence string 0 (.start m))
             (replacement (re-groups m))
             (.subSequence string (.end m) (.length string)))
        string))
    (.. regex (matcher string) (replaceFirst replacement))))

(defmethod re-sub clojure.lang.PersistentList
  [#^String input-string regex-pattern-pairs]
  (let [reversed (reverse regex-pattern-pairs)
	pair (first reversed)
	remaining (rest reversed)]
    (if (empty? remaining)
      (re-sub input-string (first pair) (second pair))    
      (re-sub (re-sub input-string (reverse remaining)) (first pair) (second pair)))))

;;; Parsing Helpers
(defmulti str-take (fn[parameter & remaining] (class parameter)))

(defmethod str-take java.util.regex.Pattern
  ([parameter input-string]
    (str-take parameter input-string {}))
  ([parameter input-string options-map]
     (let [matches (re-partition input-string parameter)]
       (if (options-map :include)
	 (apply str (take 2 matches))
	 (first matches)))))

(defmethod str-take :default
  [parameter input-string]
  (apply str (take parameter input-string)))

(defn str-rest
  [#^String input-string]
  (apply str (rest input-string)))

(defmulti str-drop (fn[parameter & remaining] (class parameter)))

(defmethod str-drop java.util.regex.Pattern
  ([parameter input-string]
    (str-drop parameter input-string {}))
  ([parameter input-string options-map]
     (let [matches (re-partition input-string parameter)]
       (if (options-map :include)
	 (apply str (rest matches))
	 (apply str (drop 2 matches))))))

(defmethod str-drop :default
  [parameter input-string]
  (apply str (drop parameter input-string)))

(defn str-before [#^String input-string #^java.util.regex.Pattern regex]
  (let [matches (re-partition input-string regex)]
    (first matches)))

(defn str-before-inc [#^String input-string #^java.util.regex.Pattern regex]
  (let [matches (re-partition input-string regex)]
    (apply str (take 2 matches))))

(defn str-after [#^String input-string #^java.util.regex.Pattern regex]
  (let [matches (re-partition input-string regex)]
    (apply str (drop 2 matches))))

(defn str-after-inc [#^String input-string #^java.util.regex.Pattern regex]
  (let [matches (re-partition input-string regex)]
    (apply str (rest matches))))

(defn str-reverse
  "This method excepts a string and returns the reversed string as a results"
  [#^String input-string]
  (apply str (reverse input-string)))

(defn str-join
  "Returns a string of all elements in 'sequence', separated by
  'separator'.  Like Perl's 'join'."
  [separator sequence]
  (apply str (interpose separator sequence)))

;;; Inflectors
;;; These methods only take the input string.
(defn upcase 
  "Converts the entire string to upper case"
  [#^String input-string]
  (. input-string toUpperCase))

(defn downcase 
  "Converts the entire string to lower case"
  [#^String input-string]
  (. input-string toLowerCase))

(defn trim
  "Shortcut for String.trim"
  [#^String input-string]
  (. input-string trim))

(defn strip
  "Alias for trim, like Ruby."
  [#^String input-string]
  (trim input-string))

(defn ltrim
  "This method chops all of the leading whitespace."
  [#^String input-string]
  (str-drop #"\s+" input-string))

(defn rtrim
  "This method chops all of the trailing whitespace."
  [#^String input-string]
  (str-reverse (str-drop #"\s+" (str-reverse input-string))))

(defn chop
  "Removes the last character of string."
  [#^String input-string]
  (subs input-string 0 (dec (count input-string))))

(defn chomp
  "Removes all trailing newline \\n or return \\r characters from
  string.  Note: String.trim() is similar and faster."
  [#^String input-string]
  (str-take #"[\r\n]+" input-string))

(defn capitalize
  "This method turns a string into a capitalized version, Xxxx"
  [#^String input-string]
  (str-join "" (list 
		(upcase (str (first input-string)))
		(downcase (str-rest input-string)))))

(defn titleize
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  Each word is capitalized, and the result is joined with \" \"."
  [#^String input-string]
  (let [words (re-split input-string #"[\s_-]+")]
    (str-join " " (map capitalize words))))

(defn camelize
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  The first word is captialized, and the rest are downcased, and the result is joined with \"\"."
  [#^String input-string]
  (let [words (re-split input-string #"[\s_-]+")]
    (str-join "" (cons (downcase (first words)) (map capitalize (rest words))))))

(defn dasherize
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  Each word is downcased, and the result is joined with \"-\"."
  [#^String input-string]
  (let [words (re-split input-string #"[\s_-]+")]
    (str-join "-" (map downcase words))))

(defn underscore
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  Each word is downcased, and the result is joined with \"_\"."
  [#^String input-string]
  (let [words (re-split input-string #"[\s_-]+")]
    (str-join "_" (map downcase words))))

(defn keywordize
  [input-string]
  (str (trim (dasherize (re-gsub (downcase input-string) #"[\(\)\"\'\:\#]" "")))))

;;;The code for the singularize function was based on functions contributed by Brian Doyle and John Hume
(defn singularize
  "This is an early attempt at Rails' singulaize method."
  [#^String input-string]
  (let [lc (downcase input-string)]
    (cond
      (.endsWith lc "ies") (re-sub lc #"ies$" "y")
      (.endsWith lc "es") (re-sub lc #"es$" "")
      :else (re-sub lc #"s$" ""))))
 
;;;The code for the pluralize function was based on functions contributed by Brian Doyle and John Hume
(defn pluralize
  "This is an early attempt at Rails' pluralize method."
  [#^String input-string]
  (let [lc (downcase input-string)]
    (cond
      (.endsWith lc "y") (re-sub lc #"y$" "ies")
      (some #(.endsWith lc %) ["s" "z" "ch" "sh" "x"]) (str lc "es")
      :else (str lc "s"))))

(defn swap-letters
  [#^String input-string]
  (cond
   (< (count input-string) 2) '()
   'true ((fn step
	    [#^String head #^String tail]
	    (let [a (first tail)
		  b (second tail)]
	      (if (nil? b)
		'() 
		(cons (str head b a (apply str (drop 2 tail))) 
		      (step 
		       (str head a) 
		       (rest tail))))))
	  "" input-string)))

(defn try-letter
  [#^String letter #^String input-string]
  ((fn insert [#^String head #^String tail] 
    (if (first tail)
      (cons (str head letter (apply str (rest tail)))
       (cons (str head letter tail)
	     (insert (str head (first tail)) (apply str (rest tail)))))
      (list (str head letter))))
  "" input-string))

;;; The nearby method
(defn nearby
  "The intent of this method is to aid spellchecking.  This method generates
a set of nearby strings.  It takes an optional sequence that can be used as 
potential missing strings.  The default is a-z, and is intially geared towards
english speakers."
  ([#^String input-string] (nearby input-string (cons "" "etaoinshrdlcumwfgypbvkjxqz")))
  ([#^String input-string replacement-seq]
     (apply concat (swap-letters input-string)
	    (map #(try-letter % input-string) replacement-seq))))


;;; Escapees

;(defn sql-escape[x])

(defn html-escape
  "This function helps prevent XSS attacks, by disallowing certain charecters"
  [#^String input-string]
  (let [escaped-charecters '((#"&" " &amp; ")
			  (#"<" " &gt; ")
			  (#">" " &lt; ")
			  (#"\"" " &quot; "))]
    (re-gsub input-string escaped-charecters)))

(defn javascript-escape
  "This function helps prevent XSS attacks, by disallowing certain charecters"
  [#^String input-string]
  (let [escaped-charecters '((#"&" "\u0026")
			  (#"<" "\u003C")
			  (#">" "\u003E"))]
    (re-gsub input-string escaped-charecters)))

;(defn pdf-escape[x])
