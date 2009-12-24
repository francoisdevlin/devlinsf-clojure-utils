(ns lib.sfd.same-test.clj
  (:use lib.sfd.core
	lib.sfd.seq-utils
	clojure.test))

;;Standard fixtures
(def inv-compare (comp - compare))
(def test-str "abcdefgh")
(def test-vec (vec test-str))
(def test-seq (take 8 test-str))

(def test-hash-set (set test-str))
(def test-sort-set (apply sorted-set test-str))
(def test-sort-set-inv (apply sorted-set-by inv-compare test-str))

(def test-hash-map (apply hash-map test-str))
(def test-sort-map (apply sorted-map test-str))
(def test-sort-map-inv (apply sorted-map-by inv-compare test-str))

(defn dirty-upcase [c]
  (char (- (int c) 32)))

;;; Unodered tests
; cons
; Yes, conj is the idiomatic way to do this.
(deftest test-cons
  (are [input result] (= (same cons \z input) result)
       test-str "zabcdefgh"
       test-vec [\z \a \b \c \d \e \f \g \h]
       test-seq '(\z \a \b \c \d \e \f \g \h)

       test-hash-set #{\z \a \b \c \d \e \f \g \h}
       test-sort-set (sorted-set \z \a \b \c \d \e \f \g \h)
       test-sort-set-inv (sorted-set-by inv-compare \z \a \b \c \d \e \f \g \h))

  (are [input result] (= (same cons [\y \z] input) result)
       test-hash-map {\a \b \c \d \e \f \g \h \y \z}
       test-sort-map (sorted-map \a \b \c \d \e \f \g \h \y \z)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d \e \f \g \h \y \z)
       ))


; map
(deftest test-map
  (are [input result] (= (same map dirty-upcase input)
			 result)
       test-str "ABCDEFGH"
       test-vec [\A \B \C \D \E \F \G \H]
       test-seq '(\A \B \C \D \E \F \G \H)

       test-hash-set #{\A \B \C \D \E \F \G \H}
       test-sort-set (sorted-set \A \B \C \D \E \F \G \H)
       test-sort-set-inv (sorted-set-by inv-compare \A \B \C \D \E \F \G \H)
       ))

(deftest test-map-key-entry
  (are [input result] (= (same map (key-entry dirty-upcase) input)
			 result)
       test-hash-map {\A \b \C \d \E \f \G \h}
       test-sort-map (sorted-map \A \b \C \d \E \f \G \h)
       test-sort-map-inv (sorted-map-by inv-compare \A \b \C \d \E \f \G \h)
       ))

(deftest test-map-val-entry
  (are [input result] (= (same map (val-entry dirty-upcase) input)
			 result)
       test-hash-map {\a \B \c \D \e \F \g \H}
       test-sort-map (sorted-map \a \B \c \D \e \F \g \H)
       test-sort-map-inv (sorted-map-by inv-compare \a \B \c \D \e \F \g \H)
       ))
  
; filter
(deftest test-filter
  (are [input result] (= (same filter #{\a \c} input)
			 result)
       test-str "ac"
       test-vec [\a \c]
       test-seq '(\a \c)

       test-hash-set #{\a \c}
       test-sort-set (sorted-set \a \c)
       test-sort-set-inv (sorted-set-by inv-compare \a \c)
))

(deftest test-filter-key
  (are [input result] (= (same filter (comp #{\a \c} key) input)
			 result)
       test-hash-map {\a \b \c \d}
       test-sort-map (sorted-map \a \b \c \d)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d)))

(deftest test-filter-val
  (are [input result] (= (same filter (comp #{\b \d} val) input)
			 result)
       test-hash-map {\a \b \c \d}
       test-sort-map (sorted-map \a \b \c \d)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d)))
       
; remove
(deftest test-remove
  (are [input result] (= (same remove (complement #{\a \c}) input)
			 result)
       test-str "ac"
       test-vec [\a \c]
       test-seq '(\a \c)

       test-hash-set #{\a \c}
       test-sort-set (sorted-set \a \c)
       test-sort-set-inv (sorted-set-by inv-compare \a \c)
))

(deftest test-remove-key
  (are [input result] (= (same remove (complement (comp #{\a \c} key)) input)
			 result)
       test-hash-map {\a \b \c \d}
       test-sort-map (sorted-map \a \b \c \d)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d)))

(deftest test-remove-val
  (are [input result] (= (same remove (complement (comp #{\b \d} val)) input)
			 result)
       test-hash-map {\a \b \c \d}
       test-sort-map (sorted-map \a \b \c \d)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d)))

; replace
; TO DO

;;; Ordered tests
;;; Should NOT work consistently for hash-maps & hash-sets
; rest
(deftest test-rest
  (are [input result] (= (same rest input) result)
       test-str "bcdefgh"
       test-vec [\b \c \d \e \f \g \h]
       test-seq '(\b \c \d \e \f \g \h)

       test-sort-set (sorted-set \b \c \d \e \f \g \h)
       test-sort-set-inv (sorted-set-by inv-compare \a \b \c \d \e \f \g)

       test-sort-map (sorted-map \c \d \e \f \g \h)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d \e \f)
       ))

; take
(deftest test-take
  (are [input result] (= (same take 2 input) result)
       test-str "ab"
       test-vec [\a \b]
       test-seq '(\a \b)

       test-sort-set (sorted-set \a \b)
       test-sort-set-inv (sorted-set-by inv-compare \g \h)

       test-sort-map (sorted-map \a \b \c \d)
       test-sort-map-inv (sorted-map-by inv-compare \e \f \g \h)
       ))

; take-last
(deftest test-take-last
  (are [input result] (= (same take-last 2 input) result)
       test-str "gh"
       test-vec [\g \h]
       test-seq '(\g \h)

       test-sort-set (sorted-set \g \h)
       test-sort-set-inv (sorted-set-by inv-compare \a \b)

       test-sort-map (sorted-map \e \f \g \h)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d)
       ))

; take-while
(deftest test-take-while
  (are [input result] (= (same take-while (set "ab") input) result)
       test-str "ab"
       test-vec [\a \b]
       test-seq '(\a \b)

       test-sort-set (sorted-set \a \b)
       test-sort-set-inv (sorted-set-by inv-compare))
  (are [input result] (= (same take-while (comp (set "ab") key) input) result)
       test-sort-map (sorted-map \a \b)
       test-sort-map-inv (sorted-map-by inv-compare))
  (are [input result] (= (same take-while (comp (set "ab") val) input) result)
       test-sort-map (sorted-map \a \b)
       test-sort-map-inv (sorted-map-by inv-compare)))

; take-until
(deftest test-take-until
  (are [input result] (= (same take-until (set "cd") input) result)
       test-str "ab"
       test-vec [\a \b]
       test-seq '(\a \b)

       test-sort-set (sorted-set \a \b)
       test-sort-set-inv (sorted-set-by inv-compare \e \f \g \h))
  (are [input result] (= (same take-until (comp (set "cd") key) input) result)
       test-sort-map (sorted-map \a \b)
       test-sort-map-inv (sorted-map-by inv-compare \e \f \g \h))
  (are [input result] (= (same take-until (comp (set "cd") val) input) result)
       test-sort-map (sorted-map \a \b)
       test-sort-map-inv (sorted-map-by inv-compare \e \f \g \h)))

; take-nth
(deftest test-take-nth
  (are [input result] (= (same take-nth 2 input) result)
       test-str "aceg"
       test-vec [\a \c \e \g]
       test-seq '(\a \c \e \g)

       test-sort-set (sorted-set \a \c \e \g)
       test-sort-set-inv (sorted-set-by inv-compare \b \d \f \h)

       test-sort-map (sorted-map \a \b \e \f)
       test-sort-map-inv (sorted-map-by inv-compare \c \d \g \h)
       ))

;drop
(deftest test-drop
  (are [input result] (= (same drop 2 input) result)
       test-str "cdefgh"
       test-vec [\c \d \e \f \g \h]
       test-seq '(\c \d \e \f \g \h)

       test-sort-set (sorted-set \c \d \e \f \g \h)
       test-sort-set-inv (sorted-set-by inv-compare \a \b \c \d \e \f)

       test-sort-map (sorted-map \e \f \g \h)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d)
       ))

; drop-last
(deftest test-drop-last
  (are [input result] (= (same drop-last 2 input) result)
       test-str "abcdef"
       test-vec [\a \b \c \d \e \f]
       test-seq '(\a \b \c \d \e \f)

       test-sort-set (sorted-set \a \b \c \d \e \f)
       test-sort-set-inv (sorted-set-by inv-compare \c \d \e \f \g \h)

       test-sort-map (sorted-map \a \b \c \d)
       test-sort-map-inv (sorted-map-by inv-compare \e \f \g \h)
       ))

; drop-while
(deftest test-drop-while
  (are [input result] (= (same drop-while (set "ab") input) result)
       test-str "cdefgh"
       test-vec [\c \d \e \f \g \h]
       test-seq '(\c \d \e \f \g \h)

       test-sort-set (sorted-set \c \d \e \f \g \h)
       test-sort-set-inv (sorted-set-by inv-compare \a \b \c \d \e \f \g \h))

  (are [input result] (= (same drop-while (comp (set "ab") key) input) result)
       test-sort-map (sorted-map \c \d \e \f \g \h)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d \e \f \g \h))

  (are [input result] (= (same drop-while (comp (set "ab") val) input) result)
       test-sort-map (sorted-map \c \d \e \f \g \h)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d \e \f \g \h)))

; drop-until
(deftest test-drop-until
  (are [input result] (= (same drop-until (set "cd") input) result)
       test-str "cdefgh"
       test-vec [\c \d \e \f \g \h]
       test-seq '(\c \d \e \f \g \h)

       test-sort-set (sorted-set \c \d \e \f \g \h)
       test-sort-set-inv (sorted-set-by inv-compare \a \b \c \d))

  (are [input result] (= (same drop-until (comp (set "cd") key) input) result)
       test-sort-map (sorted-map \c \d \e \f \g \h)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d))

  (are [input result] (= (same drop-until (comp (set "cd") val) input) result)
       test-sort-map (sorted-map \c \d \e \f \g \h)
       test-sort-map-inv (sorted-map-by inv-compare \a \b \c \d)))


;;; Ordered, unsorted
;;; Should NOT work for sorted-map & sorted-sets
; reverse
(deftest test-reverse
  (are [input result] (= (same reverse input) result)
       test-str "hgfedcba"
       test-vec [\h \g \f \e \d \c \b \a]
       test-seq '(\h \g \f \e \d \c \b \a)))

; sort
(deftest test-sort
  (are [input result] (= (same sort input) result)
       "hgfedcba" test-str
       [\h \g \f \e \d \c \b \a] test-vec)
  (are [input result] (= (same sort inv-compare input) result)
       test-str "hgfedcba"
       test-vec [\h \g \f \e \d \c \b \a]
       ))

;sort-by
(deftest test-sort-by
  (are [input result] (= (same sort-by dirty-upcase input) result)
       "hgfedcba" test-str
       [\h \g \f \e \d \c \b \a] test-vec)
  (are [input result] (= (same sort-by dirty-upcase inv-compare input) result)
       test-str "hgfedcba"
       test-vec [\h \g \f \e \d \c \b \a]))

; rotate
(deftest test-rotate
  (are [input result] (= (same rotate 1 input) result)
       test-str "bcdefgha"
       test-vec [\b \c \d \e \f \g \h \a]
       test-seq '(\b \c \d \e \f \g \h \a)))

; rotate-while
(deftest test-rotate-while
  (are [input result] (= (same rotate-while (set "ab") input) result)
       test-str "cdefghab"
       test-vec [\c \d \e \f \g \h \a \b]
       test-seq '(\c \d \e \f \g \h \a \b)))

; rotate-until
(deftest test-rotate-until
  (are [input result] (= (same rotate-until (set "cd") input) result)
       test-str "cdefghab"
       test-vec [\c \d \e \f \g \h \a \b]
       test-seq '(\c \d \e \f \g \h \a \b)))

;;;String only stuff
; repeat
(deftest test-repeat
  (are [input result] (= (same repeat 2 input) result)
       test-str "abcdefghabcdefgh"))