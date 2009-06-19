Here's my solution to the problem.  It's a bit long winded, so bear 
with me (or ignore it :)) 

#trans

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

	user=> ((trans :a #(inc (% :a))) test-map) 
	{:a 1, :b "B", :c "C"} 

#deftrans

trans is a little cumbersome, generating a closure.  I also wrote a 
deftrans macro.  It creates a trans and stores it in the provided 
name: 

	user=> (deftrans counter :count count) 
	#'user/counter 
	
	user=> (counter test-map) 
	{:count 3, :a 0, :b "B", :c "C"} 
	
	user=> (deftrans inc-a :a #(inc (% :a))) 
	#'user/inc-a 

	user=> (inc-a test-map) 
	{:a 1, :b "B", :c "C"} 

#Using a closure
	
Let's revisit the fact that trans generates a closure.  We can use the 
resulting transform anywhere we'd use a function. 

## In a map
	user=> (map (trans :count count) (repeat 5 test-map)) 
	({:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"}) 

Or, we could use the def'd version 

	user=> (map counter (repeat 5 test-map)) 
	(...) 

##In a comp
	user=> ((comp inc-a counter counter) test-map) 
	{:count 4, :a 1, :b "B", :c "C"} 

##In the STM

	user=> (def test-ref (ref test-map)) 
	#'user/test-ref 

	user=> (dosync(alter test-ref inc-a)) 
	{:a 1, :b "B", :c "C"} 

	user=> @test-ref 
	{:a 1, :b "B", :c "C"} 
	
#Extra stuff 

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
	
That's it for now folks.  I leave it to you to consider what this is 
good for.  Personally, I like using this to help me transform database 
data.