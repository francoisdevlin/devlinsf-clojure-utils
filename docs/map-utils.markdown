#map-utils

Sean Devlin

July 1, 2009

Namespace: lib.devlinsf.map-utils

This is a collection of map utility functions that I use when manipulating map data.  Since much of clojure is designed around manipulating a list of tuples, 
I find these very handy.  This documentation reviews how to do the following things.

* transforming a map
* projecting a map
* marshalling a map
* joining map lists
* pivoting map lists

#Altering a map
All the higher order functions in clojure accept and return a seq.  It is common to transform the resulting seq into a hash map.
These are a few functions that do this for you automatically.

Let's create a map for example purposes.

	user=> (def abc123 {"a" 1 "b" 2 "c" 3})
	#'user/abc123	

##map-vals
This is like the `map` operator, but it applies `f` to every value of the hash map instead of the entry.  It returns a hash map.

	user=> (map-vals #(* 2 %) abc123)
	{"c" 6, "b" 4, "a" 2}

##map-keys
This is like the `map` operator, but it applies `f` to every key of the hash map instead of the entry.  It returns a hash map.

	user=> (map-keys keyword abc123)
	{:c 3, :b 2, :a 1}

However, since this function modifies keys, it is possible to generate a collision

	user=> (map-keys (constantly :collide) abc123)
	{:collide 3}
	
To get around this, it is possible to pass an optional reduction function, that works like merge-with

	user=> (map-keys (constantly :collide) + abc123)
	{:collide 6}

##filter-map
This behaves just like `filter`.  `pred` is applied to each entry of the hash-map, and the resulting collection is transformed into a hash map.

	user=> (filter-map (comp even? second) abc123)
	{"b" 2}

##remove-map
This behaves just like `remove`.  `pred` is applied to each entry of the hash-map, and the resulting collection is transformed into a hash map.

	user=> (remove-map (comp even? second) abc123)
	{"a" 1, "c" 3}

#Tranforming a map

This section explores the `trans` closure, which is used to modify a map.

##trans

I defined a function trans 

	(defn trans [& params]...) 

Let me show an example: 
	
	user=> ((trans :count count) abc123) 
	{:count 3, "a" 1, "b" 2, "c" 3}
	
Notice the call to trans first, and then the result it applied to test- 
map.  This is because trans generates a closure.  In this case, it 
applies the count function to the map, and associates it with the 
key :count. 

	user=> ((trans "a" (comp inc #(get % "a"))) abc123) 
	{:a 1, :b "B", :c "C"} 

##deftrans

trans is a little cumbersome, generating a closure.  I also wrote a 
deftrans macro.  It creates a trans and stores it in the provided 
name: 

	user=> (deftrans counter :count count) 
	#'user/counter 
	
	user=> (counter abc123) 
	{:count 3, "a" 1, "b" 2, "c" 3}
	
	user=> (deftrans inc-a :a (comp inc :a)) 
	#'user/inc-a 

	user=> (inc-a abc123) 
	{:a 1, :b "B", :c "C"} 

##Using a closure
	
Let's revisit the fact that trans generates a closure.  We can use the 
resulting transform anywhere we'd use a function. 

### In a map

	user=> (map counter (repeat 5 abc123)) 
	({:count 3, "a" 1, "b" 2, "c" 3}
	 {:count 3, "a" 1, "b" 2, "c" 3}
	 {:count 3, "a" 1, "b" 2, "c" 3}
	 {:count 3, "a" 1, "b" 2, "c" 3}
	 {:count 3, "a" 1, "b" 2, "c" 3})


### In a comp
	user=> ((comp counter counter) abc123)
	{:count 4, "a" 1, "b" 2, "c" 3}

### In the STM

This is my favorite use of trans so far

	user=> (def test-ref (ref abc123)) 
	#'user/test-ref 

	user=> (dosync (alter test-ref counter)) 
	{:count 3, "a" 1, "b" 2, "c" 3}

	user=> @test-ref 
	{:count 3, "a" 1, "b" 2, "c" 3}
	
##trans*

The trans function associates its values after all the functions have been evaluated

	user=> ((trans :c1 count :c2 count :c3 count) abc123)
	{:c3 3, :c2 3, :c1 3, "a" 1, "b" 2, "c" 3}

I have also defined the trans* closure, which associates the value in the map between each iteration.  I believe 
this mimic the distinction between let & let* in CL, but I am unsure.
	
	user=> ((trans* :c1 count :c2 count :c3 count) abc123)
	{:c3 5, :c2 4, :c1 3, "a" 1, "b" 2, "c" 3}

#marshall-hashmap
This is designed to aid in transforming a list into a hash-map.  Typically a list of pairs need a minor transformation to become a hash-map.
###Signature

	(marshall-hashmap coll)
 	(marshall-hashmap coll key-fn)
 	(marshall-hashmap coll key-fn val-fn)
 	(marshall-hashmap coll key-fn val-fn merge-fn)

This is a function designed to marsh a hash-map from a collection.  Very handy to combine with a parser.  The defaults are

	key-fn: first
	val-fn: second
	merge-fn: merge-like

They are progressively replaced as the arity increases.

###Usage
TO DO: Add an example


##freq
`freq` is a special mapping function.  It constantly returns 1.  This is to enable counting in the pivot method.

##pivot
`pivot` has the following signature:

	(pivot coll grouping-fn & fns)

It is designed to take an alternating list of mapping and reducing functions.  Let's use the test-right data above as an example.

	user=> (pivot test-right :owner (comp count :item) +)
	{"Matt" [4], "Ross" [8], "Sean" [9]}

The data is grouped by :owner, each row is mapped by the `(comp count :item)` function (it computes the length of item), 
and the resulting lists are reduced by the `+` function.

1. How the data is grouped.
2. How each hash is mapped.
3. How each mapping is reduced.

