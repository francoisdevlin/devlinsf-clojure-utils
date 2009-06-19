#Predicate Utilities

Sean Devlin

June 12 2008

Namespace: lib.devlinsf.predicate-utils

This is a namespace to assist with predicate creation and composition in clojure.  The AND & OR macros are good for testing, but not filtering.
This library attempts to fill those gaps.

#(match-map[ & params]...)

This function creates a predicate from a hash-map.

	user=> 	(def test-map 
				{:name "Sean" 
				:job "Software Nerd" 
				:gender :male 
				:favorite-team "Pittsburgh Steelers"})
				
Let look at an example:

	user=> ((match-map {:name "Sean"}) test-map)
	true
				
The same entries method created a filter that determined if its input had a :name key of "Sean".  Since `test-map` does have such a key, it returned true.  Let's look at a
second example.

	user=> ((match-map {:favorite-team "Pittsburgh Steelers" :gender :male}) test-map)
	true
	
This time we were looking for two entries.  Since the `test-map` has the exact same values for each of the keys, it matched.  Look at a third example:

	user=> ((match-map {:job "Software Nerd" :gender :female}) test-map)
	false
	
While our test-map is a Software Nerd, it isn't female.  The test fails.
	
This function can also take a list of keys
	
	user=> ((match-map :name "Sean") test-map)
	true

##Testing Mutliple Values
	
This function also supports testing if a value is in a vector of values

	user=> ((match-map {:gender [:male :female]}) test-map)
	true

##match-map-strict[ & params]
	
This method tests for strict equality on keys.  It is designed to be used when the vector expansion behavior is to be avoided.

	user=> ((match-map-strict {:gender [:male :female]}) test-map)
	false
	
#(every-pred? [& predicates]...)

This function composes a list of predicates with a logical AND.  Look at the following example:

	user=> ((every-pred?
				(match-map {:job "Software Nerd"}) 
				#(= (count %) 4)) 
			test-map)
	true

This is primarily useful for composing predicates for filtering operations.  It is based on the `every?` method, so it fails fast.

#(any-pred? [& predicates]...)	

This function composes a list of predicates with a logical OR.  Look at the following example:

	user=> ((any-pred? 
					(match-map {:job "Software Nerd"}) 
					(match-map {:gender :female}))
			test-map)
	true

This is primarily useful for composing predicates for filtering operations.  It is based on the `some` method, so it succeeds fast.

#(some-pred? [& predicates])

This method behaves like `any-pred?`, but returns the value of the some predicate, not a true/false value.