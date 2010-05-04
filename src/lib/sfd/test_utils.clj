(ns lib.sfd.test-utils
  (:use clojure.test))

(defmacro defspec
  "This macro is designed to define an abstract specification, that
accepts specific implementations based on bindings.  Designed to test
interfaces & protocols for invariant behavior."
  [name bindings & body]
  `(def ~name (quote [~bindings ~@body])))

(defmacro enforce
  "This is what actually enforces an abtract specification.  A test case
is created with the name mangled based on the spec and implementation."
  [spec & impl]
  (let [test-name (symbol (apply str (name spec) "-" (map name impl)))]
    `(let ~(vec (interleave (first (eval spec)) impl))
       (deftest ~test-name
	 ~@(rest (eval spec))))))


;;----------------
;; Example
;;----------------
(defspec rotate-spec
    [rotate-imp]
  (are [n] (= (rotate-imp n []) '())
       -6 -5 -4 -3 -2 -1 0 1 2 3 4 5 6)
  (are [n] (= (count (rotate-imp n test-vec)) (count test-vec))
       -6 -5 -4 -3 -2 -1 0 1 2 3 4 5 6)
  (are [n coll] (= (rotate-imp n test-vec) (seq coll))	
       -6 [5 1 2 3 4]
       -5 [1 2 3 4 5]
       -4 [2 3 4 5 1]
       -3 [3 4 5 1 2]
       -2 [4 5 1 2 3]
       -1 [5 1 2 3 4]
       0 [1 2 3 4 5]
       1 [2 3 4 5 1]
       2 [3 4 5 1 2]
       3 [4 5 1 2 3]
       4 [5 1 2 3 4]
       5 [1 2 3 4 5]
       6 [2 3 4 5 1]))

;Would be called like so
;(enforce rotate-spec a-specific-implementation)
