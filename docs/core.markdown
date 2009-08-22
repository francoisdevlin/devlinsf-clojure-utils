#Core Utilities

Sean Devlin 

June 11, 2009

Namespace: lib.devlinsf.core

This namespace is for core extension methods to clojure.  These are functions so broad that I think they should be always included in a namespace.

#aliases

#&
The & symbol is an alias for comp

#p
The p symbol is an alias for partial

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

#fn-tuple

In order to understand fn-tuple, I'd like to first talk about comp 
(osition).  Comp can be defined in terms of reduce like so: 

	(defn my-comp [& fns] 
  		(fn [args] 
    		(reduce 
      			(fn[accum f](f accum)) 
      			(conj (reverse (seq fns)) args)))) 

Granted, this isn't 100% equivalent to the Clojure comp function, but 
it is very, very close.  What it demonstrates is that comp applies a 
list of functions in series using reduce.  After writing Clojure for a 
while, I found frequent need to apply a list of functions in parallel 
using map.  fn-tuple can be defined as follows 

	(defn fn-tuple [& fns] 
  		(fn[& args] (map #(apply % args) fns))) 

Notice that fn-tuple creates a closure.  Initially, I used this as a way 
to access values from a map. 

	user=>(def test-map {:a "1" :b "2" :c "3" :d "4"}) 
	user=>((fn-tuple :a :c) test-map) 
	("1" "3") 
	
However, as I used fn-tuple more and more, I found it to be a useful way 
perform many operations on a map at once.  For example 
assume parse-int turns a string to an int appropriately 

	user=>((fn-tuple :a (comp parse-int :c)) test-map) 
	("1" 3) 
	
Since fn-tuple returns a closure, it is very useful in any place I would 
use a map operation as well.  For example, this made turning a list of 
maps into a list of lists very easy. Also, this made it very easy to 
determine if a sub-selection of a hash-map is equal to another hash- 
map 

	user=>(def test-fn-tuple (fn-tuple :a :c)) 
	user=>(= (test-fn-tuple {:a 1 :b 2 :c 3}) (test-fn-tuple {:a 1 :b 34 :c 3})) 
	true 
	
One thing that is very interesting is that this function allows me to 
simulate the behavior of let in a point-free style.  This is something 
I am still experimenting with. 

	;This is deliberate overkill for a small example 
	;Generate a list of squares 
	;Notice that the fn-tuple uses the range twice 
	user=>((partial map (fn-tuple identity #(* % %))) 
           (range 1 6)) 
		((1 1) (2 4) (3 9) (4 16) (5 25)) 