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


That's it for now folks.  I leave it to you to consider what this is 
good for.  Personally, I like using this to help me transform database 
data.