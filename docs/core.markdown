#Core Utilities

Sean Devlin 

June 11, 2009

Namespace: lib.devlinsf.core

This namespace is for core extension methods to clojure.  These are functions so broad that I think they should be always included in a namespace.


#apply-map

I've been using Meikel's defnk macro a fair bit in some of my code.  I 
have several functions that have sensible defaults that only need to 
be overridden occasionally, so keyword arguments fit the bill nicely. 
Let's consider two (contrived) keyword fns. 

	(use 'clojure.contrib.def) 

	(defnk add 
  		[x :y 10 :z 20] 
  		(+ x y z)) 

	(defnk nil-free? 
  		[a :b 1 :c 2] 
  		(every? (comp not nil?) [a b c])) 

There are several occasions I would like to use apply, and supply a 
map for the keyword arguments.  For those of you with a Python 
background, it would work just like the ** operator.  Let's call this 
function apply-map (a definition can be found here: http://gist.github.com/159324 
).  You can use apply-map just like you would apply. 

	(def useful-vec [:y 0]) 
	(def useful-map {:y 0}) 

	user=> (apply add 1 usefull-vec) 
	21 

	;This could be used to apply setting from a user... 
	user=>(apply-map add 1 useful-map) 
	21 
	
Some more use cases: 

	user=> (def test-map {:a "A" :b "B" :c "C"}) 
	#'user/test-map 
	
	user=> (vector test-map) 
	[{:a "A", :b "B", :c "C"}] 
	
	user=> (apply vector test-map) 
	[[:a "A"] [:b "B"] [:c "C"]] 
	
	user=> (apply-map vector test-map) 
	[:a "A" :b "B" :c "C"] 
	
This helps with higher order functions 

	;This worked before, because Meikel is just awesome. 
	user=>(map add [1 2 3]) 
	(31 32 33) 

	;Slightly ugly, but it works... 
	user=>(map 
               #(apply-map add (first %) (second %)) 
               [[1 {:z 0}] [2 {:y 0}] [3]]) 
	(11 22 33) 
	
Similarly, this works for filter/remove 

	;Again, somewhat ugly... 
	user=>(filter 
             #(apply-map nil-free? (first %) (second %)) 
             [[nil {}] [1 {:b nil}] [2]]) 
	([2]) 
	
This does NOT attempt to solve the problem of mixing keyword and 
variable length arguments.  However, I think it is a step forward in 
making keyword arguments more useful in Clojure. 
