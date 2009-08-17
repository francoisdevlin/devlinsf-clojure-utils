#Sort-Utilities

Sean Devlin 

July 14, 2009

Namespace: lib.devlinsf.sort-utils

This namespace is designed to help sorting tuples.

#Create a comparator

This could be done with a macro, but I think that it could be done 
simply with closure.  Here's my attempt.  I'm going to define two 
helper functions first 

	(defn <=> 
  		"This creates a comparator that wraps the mapping fn f." 
  		[f] 
  		(fn [a b] 
    		(compare (f a) (f b)))) 

	(defn -<=> 
  		"This creates an inverse comparator that wraps the mapping fn f." 
  		[f] 
  		(fn [a b] 
    		(compare (f b) (f a)))) 

I don't quite like the use of <=> and -<=>, but it the best 
solution I could come up with that: 

* Works with a closure (frequent readers should notice a theme in my posts...) 
* Allows inverting a comparison 
* Uses the minimum amount of comparisons

#Chain comparisons
These functions take in a mapping function and return a comparator/ 
inverse comparator, respectively.  Now, I'll define a chaining 
function. 

This takes a list of comparator functions, and chains them together.  It returns another comparator. 
It behaves similar to `comp`, in that fns are applied 
right to left.

Now, to define some simple test data 

	(def test-maps 
  		[{:a 0 :b 1} 
   		{:a 0 :b 3} 
   		{:a 0 :b 2} 
   		{:a 1 :b 2}]) 

Since the chain comp returns a comparator, we'll use sort, and not 
sort-by. The first example sorts by `:a`, and then `:b`.

	user=> (sort (chain-comp (<=> :b) (<=> :a)) test-maps) 
	({:a 0, :b 1} {:a 0, :b 2} {:a 0, :b 3} {:a 1, :b 2}) 
	
This second example does the same thing, but sorts `:b` in reverse.

	user=> (sort (chain-comp (-<=> :b) (<=> :a)) test-tuples) 
	({:a 0, :b 3} {:a 0, :b 2} {:a 0, :b 1} {:a 1, :b 2}) 

#`most` and `least`

I also defined two functions, most and least, that return the maximal & minimal items in a set.

	user=> (least (chain-comp (-<=> :b) (<=> :a)) test-tuples) 
	{:a 0, :b 3}

	user=> (most (chain-comp (-<=> :b) (<=> :a)) test-tuples) 
	{:a 1, :b 2} 
