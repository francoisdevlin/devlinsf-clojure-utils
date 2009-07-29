#map-utils

Sean Devlin

July 1, 2009

Namespace: lib.devlinsf.map-utils

This is a collection of map utility functions that I use when manipulating map data.  Since much of clojure is designed around manipulating a list of tuples, 
I find these very handy.  First, we will look at transforming a map.  Then we'll take a look at project a map, followed by pivoting over a list of tuples.

#Tranforming a map

This section explores the `trans` closure, which is used to modify a tuple.

##trans

I defined a function trans 

	(defn trans [& params]...) 

Let me show an example: 

	user=> (def test-map {:a 0 :b "B" :c "C"}) 
	#'user/test-map 
	
	user=> ((trans :count count) test-map) 
	{:count 3, :a 0, :b "B", :c "C"} 
	
Notice the call to trans first, and then the result it applied to test- 
map.  This is because trans generates a closure.  In this case, it 
applies the count function to the map, and associates it with the 
key :count. 

Here's how I would write the incrementer: 

	user=> ((trans :a (comp inc :a)) test-map) 
	{:a 1, :b "B", :c "C"} 

##deftrans

trans is a little cumbersome, generating a closure.  I also wrote a 
deftrans macro.  It creates a trans and stores it in the provided 
name: 

	user=> (deftrans counter :count count) 
	#'user/counter 
	
	user=> (counter test-map) 
	{:count 3, :a 0, :b "B", :c "C"} 
	
	user=> (deftrans inc-a :a (comp inc :a)) 
	#'user/inc-a 

	user=> (inc-a test-map) 
	{:a 1, :b "B", :c "C"} 

##Using a closure
	
Let's revisit the fact that trans generates a closure.  We can use the 
resulting transform anywhere we'd use a function. 

### In a map
	user=> (map (trans :count count) (repeat 5 test-map)) 
	({:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"}) 

Or, we could use the def'd version 

	user=> (map counter (repeat 5 test-map)) 
	(...) 

### In a comp
	user=> ((comp inc-a counter counter) test-map) 
	{:count 4, :a 1, :b "B", :c "C"} 

### In the STM

This is my favorite used of trans so far

	user=> (def test-ref (ref test-map)) 
	#'user/test-ref 

	user=> (dosync (alter test-ref inc-a)) 
	{:a 1, :b "B", :c "C"} 

	user=> @test-ref 
	{:a 1, :b "B", :c "C"} 
	
##Extra stuff 

I also added a feature to enable the use of decoder/lookup maps.  When 
a map is passed instead of a function, it is assumed that the map is 
to decode the specific key it is assigned to. 

	(def decoder-map {0 "Awesome" 1 "Not Awesome"}) 

	;This will decode the key :a 
	user=> (deftrans decoder :a decoder-map) 
	#'user/decoder 
	
	user=> (decoder test-map) 
	{:a "Awesome", :b "B", :c "C"} 
	
	user=> (decoder @test-ref) 
	{:a "Not Awesome", :b "B", :c "C"} 
	
#Projecting a map
`proj` is used to apply a list of functions to a map, and return the result as a vector.

#Altering a map
All the higher order functions in clojure accept and return a seq.  It is common to transform the resulting seq into a hash map.  These are a few functions that do this for you automatically.

##map-vals
This is like the `map` operator, but it applies `f` to every value of the hash map instead of the entry.  It returns a hash map.

##map-keys
This is like the `map` operator, but it applies `f` to every key of the hash map instead of the entry.  It returns a hash map.

##filter-map
This behaves just like `filter`.  `pred` is applied to each entry of the hash-map, and the resulting collection is transformed into a hash map.

##remove-map
This behaves just like `remove`.  `pred` is applied to each entry of the hash-map, and the resulting collection is transformed into a hash map.

#Pivoting a list of tuples
This was inspired by the pivot table feature of Excel.  It is very common to have to group, map, and reduce a list of tuples.  The pivot function is designed to handle all of the 
skeleton code, so that the developer only has to worry about three things:

1. How the data is grouped.
2. How each tuple is mapped.
3. How each mapping is reduced.


##pivot
`pivot` has the following signature:

	(pivot coll grouping-fn & fns)

It is designed to take an alternating list of mapping and reducing functions.

##freq
`freq` is a special mapping function.  It constantly returns 1.  This is to enable counting in the pivot method.

