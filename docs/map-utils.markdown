#Map Utils

Sean Devlin

Dec 11, 2009

#Applying higher order functions to a map

Several times one has to use higher order functions on a map.  For example, a mapping operation needs to be applied to a 
the values of a map, or a filtering operation needs to be applied to the keys of a map.  Because of Clojure's seq abstraction,
each of this functions can be applied to a map.  However, there is often some intermediate code require to get the operation
to work just right.

This library is intended to encapsulate the glue code for you.  All of these functions are designed to implement a visitor
pattern, so that there is minimal changes in your code.  A map in, a map out.  No need to compose operations so
that they are applied to keys or value appropriately.  There are two main groups of functions.  The first group we'll 
explore is the set designed to work with predicate functions, such as filter & remove.  The second set are mapping 
functions, designed to work with map.  

For all of our examples, we'll be using the map below:

	user=> (def abc123 {"a" 1 "b" 2 "c" 3})
	#'user/abc123	


#Predicate functions

There are two main functions to work with predicate functions.

* keys-pred
* vals-pred

Let's jump into an example.  Suppose you want to filter the even values in our map.  We'll use `vals-pred` to 
modify the operation of filter appropriately.

	user=>(vals-pred filter even? abc123)
	{"b" 2}
	
Now, let's remove the even values from the same map.

	user=>(vals-pred remove even? abc123)
	{"a" 1 "c" 3}
	
Notice how the vals-pred function applies the even? predicate to the values of each entry of the map.  Also,
it works with both remove and filter.  It can be used with any predicate function, so it also works with 
take-while and drop-while (yes, there are use cases).

Let's apply a filter to the keys.  For the sake of discussion, we'll use `#{"a" "b"}` 

	user=>(keys-pred filter #{"a" "b"} abc123)
	{"a" 1 "b" 2}
	
	user=>(keys-pred remove #{"a" "b"} abc123)
	{"c" 3}

This function is used exactly the same as vals-pred, but works on the keys.  The only thing you need to 
do as a developer is keep track of is you want this to work on the values or the keys of a map.

#Mapping functions

There are often times There a three main functions for mapping functions

* vals-entry
* keys-entry
* keys-entry-merge

Let's increment every value in the map

	user=>(vals-entry map inc abc123)
	{"a" 2 "b" 3 "c" 4}
	
Okay, now, time to capitalize every key

	user=>(keys-entry map #(.toUpperCase %) abc123)
	{"A" 1 "B" 2 "C" 3}
	
And, time to force a collision.  They will be merged with merge-fn.

	user=>(keys-entry-merge + map (constantly "Example") abc123)
	{"Example" 6}
	
#More specific forms

These functions take a map in and return a map out.  However, we have not said anything about the type of map returned.  
There are ten more functions in this lib.  Five of them deliberately return a hash-map

* hash-keys-pred
* hash-vals-pred
* hash-vals-entry
* hash-keys-entry
* hash-keys-entry-merge

And the other five expect and return a sorted tree map.  The original comparator is preserved.

* sort-keys-pred
* sort-vals-pred
* sort-vals-entry
* sort-keys-entry
* sort-keys-entry-merge

Okay, great.  Which functions am I supposed to call as a developer?  You're telling me I have to keep track of this stuff?  
This sounds like more work.  Are you telling me I have to do more work to get it right?

No.  No you don't.  The first five functions you saw are adaptive, and choose the hash or sorted version 
appropriately.  The worker functions are available if needed, perhaps for a slightly faster call.
	
#Tranforming a map

This section explores the `trans` closure, which is used to modify a map.

##trans

	(defn trans [& params]...) 

Here is an example: 
	
	user=> ((trans :count count) abc123) 
	{:count 3, "a" 1, "b" 2, "c" 3}
	
Notice the call to trans first, and then the result it applied to test- 
map.  This is because trans generates a closure.  In this case, it 
applies the count function to the map, and associates it with the 
key :count. 

	user=> ((trans "a" (comp inc #(get % "a"))) abc123) 
	{:a 1, :b "B", :c "C"} 

##deftrans

trans is a little cumbersome, generating a closure.  There is also a 
deftrans macro.  It creates a trans and stores it in the provided 
name: 

	
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

	user=> (def counter (trans :count count))
	#'user/counter 

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

Also defined is the trans* closure, which associates the value in the map between each iteration.  This is
intended to mimic the distinction between let & let* in CL.
	
	user=> ((trans* :c1 count :c2 count :c3 count) abc123)
	{:c3 5, :c2 4, :c1 3, "a" 1, "b" 2, "c" 3}

	
