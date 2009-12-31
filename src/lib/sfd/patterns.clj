(ns lib.sfd.patterns)

(let [union (fn [& args] (set (apply concat args)))
      char-set (fn [start stop] (set (map char (range start stop))))
      upper (char-set 0x41 0x5B)
      lower (char-set 0x61 0x7B)
      digit (set "0123456789")
      white (set " \t\r\n\f")
      cntrl (union (char-set 0x00 0x20) [(char 0x7F)])]
  (def #^{:doc "This is a fn to get sets that match their regex patters."
	  :arglists '([regex-shorthand])}
       regex-group
       {\d digit
	\D (complement digit)
	\s white
	\S (complement white)
	\w (union upper lower digit "_")
	\W (complement (union upper lower digit "_"))
	:alnum (union upper lower digit)
	:alpha (union upper lower)
	:ascii (char-set 0x00 0x80)
	:blank (set " \t")
	:cntrl cntrl
	:digit digit
	:graph (char-set 0x21 0x7F)
	:lower lower
	:print (char-set 0x20 0x7F)
	:punct (set "!\"#$%&'()*+,\\-./:;<=>?@[]^_`{|}~")
	:space white
	:upper upper
	:word (union upper lower digit "_")
	:xdigit (union digit "abcdefABCDEF")
	}))

(defn alternate
  "Splits a collection by matching a predicate.  The predicate match is greedy.  It's like a regex partition."
  [pred coll]
  (lazy-seq
    (loop [output []
	   r-coll coll]
      (let [item (first r-coll)
	    split-coll (split-with #(= (nil? (pred item)) (nil? (pred %))) r-coll)
	    next-output (conj output (first split-coll))
	    non-matching-coll (second split-coll)]
	(if (empty? non-matching-coll)
	  next-output
	  (recur next-output non-matching-coll))))))

(defn split
  "Splits like a \\+ regex split."
  [pred coll]
  (remove (comp pred first) (alternate pred coll)))

(defn match
  "Finds all of the matches."
  [pred coll]
  (filter (comp pred first) (alternate pred coll)))

(defn replace-if
  "Replaces all values that match the predicate with the
rep(lacement) value."
  [pred rep coll]
  (map #(if (pred %) rep %) coll))

(defn map-if
  "maps f if the predicate is true.  Otherwise returns identity."
  [pred f coll]
  (map #(if (pred %) (f %) %) coll))