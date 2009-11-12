(ns lib.sfd.str-utils)

(defmulti modify (fn [& args] (class (second args))))

(defmethod modify clojure.lang.Keyword
  [f k]
  (keyword (f (name k))))

(defmethod modify clojure.lang.Symbol
  [f s]
  (symbol (f (name s))))

(defmethod modify :default
  [f s]
  (f s))

(defmacro defmod [sym docstring binding body]
  (let [star-sym (symbol (str sym "*"))
	base-doc (str "This is the base function designed to work on a string. \n\n" docstring)
	versatile-doc (str "multifn - This is the wrapped multimethod designed to work on strings, keywords or symbols. \n\n  " docstring)
	inline-f (if (= (count binding) 1)
		   star-sym
		   (concat (list 'partial star-sym) (butlast binding)))]
    `(do
      (defn ~star-sym ~base-doc ~binding ~body)
      (defn ~sym ~versatile-doc ~binding (modify ~inline-f ~(last binding))))))

;;; String Merging & Slicing

;  "Splits the string into a lazy sequence of substrings, alternating
;  between substrings that match the patthern and the substrings
;  between the matches.  The sequence always starts with the substring
;  before the first match, or an empty string if the beginning of the
;  string matches.
;
;  For example: (re-partition \"abc123def\" #\"[a-z]+\")
;
;  Returns: (\"\" \"abc\" \"123\" \"def\")"
(defn re-partition
  [#^java.util.regex.Pattern re #^String string]
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

(defn split
  [#^java.util.regex.Pattern pattern #^String input-string]
  ((fn step[input-sequence]
     (lazy-seq
       (if (first input-sequence)
	 (cons (first input-sequence) (step (drop 2 input-sequence)))
	 '())))
   (re-partition pattern input-string)))

(defmod gsub
  "Takes a pattern and replacement string, replaces every occurance in string."
  [#^java.util.regex.Pattern regex #^String replacement #^String string]
  (if (ifn? replacement)
    (let [parts (vec (re-partition regex string))]
      (apply str
             (reduce (fn [parts match-idx]
                       (update-in parts [match-idx] replacement))
                     parts (range 1 (count parts) 2))))
    (.. regex (matcher string) (replaceAll replacement))))

(defmod sub
  "Takes a pattern and replacement string, replaces the first occurance in string."
  [#^java.util.regex.Pattern regex #^String replacement #^String string]
  (if (ifn? replacement)
    (let [m (re-matcher regex string)]
      (if (.find m)
        (str (.subSequence string 0 (.start m))
             (replacement (re-groups m))
             (.subSequence string (.end m) (.length string)))
        string))
    (.. regex (matcher string) (replaceFirst replacement))))

;;; Parsing Helpers
(defmulti str-take-worker (fn[regex & remaining] (class regex)))

(defmethod str-take-worker java.util.regex.Pattern
  ([regex input-string]
    (str-take regex input-string {}))
  ([regex input-string options-map]
     (let [matches (re-partition regex input-string)]
       (if (options-map :include)
	 (apply str (take 2 matches))
	 (first matches)))))

(defmethod str-take-worker :default
  [n input-string]
  (apply str (take n input-string)))

(defmod str-take
  "str-take can take two forms of inputs in the limiter.  If limiter is set to an integer, it behave just like take, while wrapping the result in a string (sorta).  If limiter is set to a regex, take returns the string before the match."
  [limiter input]
  (str-take-worker limiter input))

(defmod str-rest
  "Returns the rest of the string"
  [#^String input-string]
  (apply str (rest input-string)))

(defmulti str-drop-worker (fn[parameter & remaining] (class parameter)))

(defmethod str-drop-worker java.util.regex.Pattern
  ([parameter input-string]
    (str-drop parameter input-string {}))
  ([parameter input-string options-map]
     (let [matches (re-partition parameter input-string)]
       (if (options-map :include)
	 (apply str (rest matches))
	 (apply str (drop 2 matches))))))

(defmethod str-drop-worker :default
  [n input-string]
  (apply str (drop n input-string)))

(defmod str-drop
  "str-drop can take two forms of inputs in the limiter.  If limiter is set to an integer, it behave just like drop, while wrapping the result in a string (sorta).  If limiter is set to a regex, drop returns the string after the match."
  [limiter input]
  (str-drop-worker limiter input))

(defn str-drop-while
  "Works like drop-while, but wraps the result into a string."
  [pred coll]
  (apply str (drop-while pred coll)))
 
(defn str-take-while
  "Works like drop-while, but wraps the result into a string."
  [pred coll]
  (apply str (take-while pred coll)))

(defmod str-reverse
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
(defmod upcase
  "Converts the entire string to upper case"
  [#^String input-string]
  (. input-string toUpperCase))

(defmod downcase
  "Converts the entire string to lower case"
  [#^String input-string]
  (. input-string toLowerCase))

(defmod trim
  "Shortcut for String.trim"
  [#^String input-string]
  (. input-string trim))

(defmod strip
  "Alias for trim, like Ruby."
  [#^String input-string]
  (trim input-string))

(defmod ltrim
  "This method chops all of the leading whitespace."
  [#^String input-string]
  (str-drop #"\s+" input-string))

(defmod rtrim
  "This method chops all of the trailing whitespace."
  [#^String input-string]
  (str-reverse (str-drop #"\s+" (str-reverse input-string))))

(defmod chop
  "Removes the last character of string."
  [#^String input-string]
  (subs input-string 0 (dec (count input-string))))

(defmod chomp
  "Removes all trailing newline \\n or return \\r characters from
  string.  Note: String.trim() is similar and faster."
  [#^String input-string]
  (str-take #"[\r\n]+" input-string))

(defmod capitalize
  "This method turns a string into a capitalized version, Xxxx"
  [#^String input-string]
  (str-join "" (list 
		(upcase (str (first input-string)))
		(downcase (str-rest input-string)))))

(defmod titleize
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  Each word is capitalized, and the result is joined with \" \"."
  [#^String input-string]
  (let [words (split #"[\s_-]+" input-string)]
    (str-join " " (map capitalize words))))

(defmod camelize
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  The first word is captialized, and the rest are downcased, and the result is joined with \"\"."
  [#^String input-string]
  (let [words (split #"[\s_-]+" input-string)]
    (str-join "" (cons (downcase (first words)) (map capitalize (rest words))))))

(defmod dasherize
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  Each word is downcased, and the result is joined with \"-\"."
  [#^String input-string]
  (let [words (split #"[\s_-]+" input-string)]
    (str-join "-" (map downcase words))))

(defmod underscore
  "This method takes an input string, splits it across whitespace, dashes, and underscores.  Each word is downcased, and the result is joined with \"_\"."
  [#^String input-string]
  (let [words (split #"[\s_-]+" input-string)]
    (str-join "_" (map downcase words))))

(defmod keywordize
  "This method takes a string and gets it ready to become a keyword."
  [#^String input-string]
  ((comp str trim dasherize (partial gsub #"[\(\)\"\'\:\#]" "") downcase)
   input-string))

(defn str->keyword
  "This method is the same as (keyword (keywordize input-string))."
  [input-string]
  (keyword (keywordize input-string)))

(def split-tabs (partial split #"[\t]"))

(def split-lines (partial split #"[\r\n]"))

(def blank? (comp zero? count))

;;;The code for the singularize function was based on functions contributed by Brian Doyle and John Hume
(defmod singularize
  "This is an early attempt at Rails' singulaize method."
  [#^String input-string]
  (let [lc (downcase input-string)]
    (cond
      (.endsWith lc "ies") (sub #"ies$" "y" lc)
      (.endsWith lc "es") (sub #"es$" "" lc)
      :else (sub #"s$" "" lc))))

;;;The code for the pluralize function was based on functions contributed by Brian Doyle and John Hume
(defmod pluralize
  "This is an early attempt at Rails' pluralize method."
  [#^String input-string]
  (let [lc (downcase input-string)]
    (cond
      (.endsWith lc "y") (sub #"y$" "ies" lc)
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

;(defn html-escape
;  "This function helps prevent XSS attacks, by disallowing certain charecters"
;  [#^String input-string]
;  (let [escaped-charecters '((#"&" " &amp; ")
;			  (#"<" " &gt; ")
;			  (#">" " &lt; ")
;			  (#"\"" " &quot; "))]
;    (gsub input-string escaped-charecters)))

;(defn javascript-escape
;  "This function helps prevent XSS attacks, by disallowing certain charecters"
;  [#^String input-string]
;  (let [escaped-charecters '((#"&" "\u0026")
;			  (#"<" "\u003C")
;			  (#">" "\u003E"))]
;    (re-gsub input-string escaped-charecters)))

;(defn pdf-escape[x])

;;;Parsers
(defn parse-double
  [input-string]
  (java.lang.Double/parseDouble input-string))

(defn parse-int
  [input-string]
  (java.lang.Integer/parseInt input-string))

;;String output
(defn to-html-table-body
  [tuple-list]
  "Expects a seq of seqs.  Turns it into an html table body."
  (map 
   (fn[row]
     (str "<tr>\n"
	  (map (fn[cell] (str "<td>" cell "</td>\n")) row)
	  "</tr>\n"))
   tuple-list))

(defn to-tab-str
  "Expects a seq of seqs.  Turns it into a tab delimited list."
  [tuple-list]
  (str-join 
   "\n"
   (map (partial str-join "\t") tuple-list)))