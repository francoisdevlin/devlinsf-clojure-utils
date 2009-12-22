(ns lib.sfd.timing)

(defmacro time*
  "Evaluates expr and prints the time it took.  Returns the time
as a double.  Takes an optional parameter n, and repeats the test
n times."
  ([expr]
     `(let [start# (. System (nanoTime))
	    ret# ~expr
	    stop# (. System (nanoTime))]
	(/ (double (- stop# start#)) 1000000.0)))
  ([n expr]
     `(map (fn[~'x] (time* ~expr )) (range 1 ~n))))