# My Proposed changes to str-utils

Sean Devlin

May 13, 2009

I've been reviewing the str-utils package, and I'd like to propose a few changes to the library. I've included the code at the bottom.

# Use Multi-Methods

I'd like to propose re-writing the following methods to used multi-methods. Every single method will take an input called input-string, and a variable set 
of inputs called remaining-inputs. The mutli-dispatch will make decide what to do based on the remaining inputs. Specifically, I've used

	(class (first remaining-inputs))

repeatedly. The two most interesting classes are java.util.regex.Pattern, and clojure.lang.PersistentList. I deliberately decided to not use sequences,
 because I believed order was important. One method takes a map as an input, but this is so that a tuple could be passed as an options hash.

## re-partion\[input-string & remaining-input\](...)

This methods behaves like the original re-partition method, with the remaining-inputs being able to a list or a pattern. It returns a lazy sequence, and
is used as a basis for for several other methods.

## re-split\[input-string & remaining-inputs\](...)

The remaining inputs can be dispatched based on a regex pattern, a list of patterns, or a map. The regex method is the basis, and does the actual work.
### Regex

This method splits a string into a list based on a regex. It depends on the re-partition method, and returns a lazy sequence.

	(re-split "1 2 3\n4 5 6" #"\n") => ("1 2 3" "4 5 6")
	
### Map

This splits each element based on the inputs of the map. It is how options are passed to the method.

    (re-split "1 2 3" {:pattern #"\s+" :offset 1}) => (2.0 3.0)

	(re-split "1 2 3" {:pattern #"\s+" :length 2}) => (1.0 2.0)

	(re-split "1 2 3" {:pattern #"\s+" :marshal-fn parse-double}) => (1.0 2.0 3.0)

The :pattern, :offset, and :length options are relatively straightforward. The :marshal-fn is mapped after the string is split.
### List

This splits each element either like a hash-map or a regex. The map operator is applied recursively to each element

	(re-split "1 2 3\n4 5 6" (list #"\n" #"\s+")) 
	=> (("1" "2" "3") ("4" "5" "6"))
	
This is equivalent to

	(map #(re-split % #"\s+") (re-split "1 2 3\n4 5 6" #"\n"))
	=> (("1" "2" "3") ("4" "5" "6"))

### Chaining

These items can be chained together, as the following example shows

    (re-split "1 2 3\n4 5 6" 
      (list #"\n" {:pattern #"\s+" :length 2 :marshal-fn parse-double}))
    => ((1.0 2.0) (4.0 5.0))

Is equivalent to:

	(map 
		#(re-split % {:pattern #"\s+" :length 2 :marshal-fn parse-double})
		(re-split "1 2 3\n4 5 6" #"\n"))
    => ((1.0 2.0) (4.0 5.0))
 
In my opinion, the `:marshal-fn` is best used at the end of the list. However, it could be used earlier in the list, but a exception will most likely be thrown.
## re-gsub\[input-string & remaining-inputs\](...)

This method can take a list or two atoms as the remaining inputs.
### Two atoms
	(re-gsub "1 2 3 4 5 6" #"\s" "") => "123456"
### A paired list
	(re-gsub "1 2 3 4 5 6" '((#"\s" "") (#"\d" "D"))) => "DDDDDD"

Note: This signature sucks.  Should be this:
	
	(re-gsub "1 2 3 4 5 6" #"\s" "" #"\d" "D")

## re-sub\[input-string & remaining-inputs\](...)

Again, this method can take a list or two atoms as the remaining inputs.
### Two atoms
	(re-sub "1 2 3 4 5 6" #"\d" "D") => "D 2 3 4 5 6"

### A paired list
	(re-sub "1 2 3 4 5 6" '((#"\d" "D") (#"\d" "E"))) => "D E 3 4 5 6"

Note: This signature sucks.  Should be this:

	(re-gsub "1 2 3 4 5 6" #"\d" "D" #"\d" "E")


#The nearby Function

The nearby function is designed to assist with a spell checker, inspired by the example from Peter Norvig.

Signatures

	(nearby [input-string])
	(nearby [input-string seq])

Here's an example.

	(nearby "cat" (seq "abc")) => 

	("act" "atc"
	"acat" "aat" "caat" "cat" "caat" "caa" "cata"
	"bcat" "bat" "cbat" "cbt" "cabt" "cab" "catb"
	"ccat" "cat" "ccat" "cct" "cact" "cac" "catc")

The resulting sequence is lazy. In order to use it in a spellchecker, try using it like this:

	(hash-set (take number (nearby "cat" (seq "abc"))))

If the function is called with only one argument, it behaves like this.

	(nearby "cat") =>
	(nearby "cat" (cons "" "etaoinshrdlcumwfgypbvkjxqz"))

The strange order was chosen because that is the english alphabet sorted by frequency. This way the earliest entries will have the highest chance of being a valid word.

# String Seq Utils

The contrib version of str-utils contains the `str-join` function. This is a string specific version of the more general interpose function. 
It inspired the creation of four other functions, `str-take`, `str-drop`, `str-rest` & `str-reverse`. The mimic the behavior of the regular 
sequence operations, with the exception that they return strings instead of a sequence. Also, some of them can alternately take a regex as an input.

## str-take

This function is designed to be similar to the take function from the core. It specifically applies the str function to the resulting sequence. 
Also, it can take a regex instead of an integer, and will take everything before the regex. Be careful not to combine a regex and a sequence, as 
this will cause an error. Finally, an optional `:include` parameter can be passed to include the matched regex.

	(str-take 7 "Clojure Is Awesome")	=>	"Clojure"
	(str-take 2 ["Clojure" "Is" "Awesome"])	=>	"ClojureIs"
	(str-take #"\s+" "Clojure Is Awesome")	=>	"Clojure"
	(str-take #"\s+" "Clojure Is Awesome" {:include true})	=>	"Clojure "
	(str-take #"\s+" ["Clojure" "Is" "Awesome"])	=>	error
	
## str-drop

This function is designed to be similar to the drop function from the core. It specifically applies the str function to the resulting sequence. Also,
 it can take a regex instead of an integer, and will take everything after the regex. Be careful not to combine a regex and a sequence, as this will
 cause an error. Finally, an optional `:include` parameter can be passed to include the matched regex.

	(str-drop 8 "Clojure Is Awesome")	=>	"Is Awesome"
	(str-drop 1 ["Clojure" "Is" "Awesome"])	=>	"IsAwesome"
	(str-drop #"\s+" "Clojure Is Awesome")	=>	"Is Awesome"
	(str-drop #"\s+" "Clojure Is Awesome" {:include true})	=>	" Is Awesome"
	(str-drop #"\s+" ["Clojure" "Is" "Awesome"])	=>	error

## str-rest

This function applies str to the rest of the input. It is equivalent to `(str-drop 1 input)`

	(str-rest (str :Clojure))`	=>	"Clojure"
## str-reverse

This methods reverses a string

	(str-reverse "Clojure") => "erujolC"
## An Example

These methods can be used to help parse strings, such as below.

	(str-take ">" (str-drop #"< h4" "< h4 ... >"))
	=> ;the stuff in the middle
	
# New Inflectors

I've added a few inflectors that I am familiar with from Rails. My apologies if their origin is anther language. I'd be interested in knowing where the method originated.  Unless it's Perl :-p

## trim

This is a convenience wrapper for the trim method java supplies

	(trim " Clojure ") => "Clojure"
## strip

This is an alias for trim. I accidently switch between trim and strip all the time.

	(strip " Clojure ") => "Clojure"
## ltrim

This method removes the leading whitespace

	(ltrim " Cloure ") => "Clojure "
## rtrim

This method removes the trailing whitespace

	(rtrim " Cloure ") => " Clojure"
## downcase

This is a convenience wrapper for the toLowerCase method java supplies

	(downcase "Clojure") => "clojure"
## upcase

This is a convenience wrapper for the toUpperCase method java supplies

	(upcase "Clojure") => "CLOJURE"
## capitalize

This method capitalizes a string

	(capitalize "clojure") => "Clojure"
## titleize, camelize, dasherize, underscore

These methods manipulate "sentences", producing a consistent output. Check the unit tests for more examples

	(titleize "clojure iS Awesome")	=>	"Clojure Is Awesome"
	(camleize "clojure iS Awesome")	=>	"clojureIsAwesome"
	(dasherize "clojure iS Awesome")	=>	"clojure-is-awesome"
	(underscore "clojure iS Awesome")	=>	"clojure_is_awesome"

## pluralize

This is an early attempt at Rails' pluralaize function. The code for the pluralize function was based on functions contributed by Brian Doyle.

	(pluralize "foo")	=>	"foos"
	(pluralize "beach")	=>	"beaches"
	(pluralize "baby")	=>	"babies"
	(pluralize "bus")	=>	"buses"

## singularize

This is an early attempt at Rails' singularize function. The code for the singulaize function was based on functions contributed by Brian Doyle.

	(singularize "foos")	=>	"foo"
	(singularize "beaches")	=>	"beach"
	(singularize "babies")	=>	"baby"
	(singularize "stops")	=>	"stop"

#Parsing Wrappers

These functions are simple wrappers for their java counterparts, with the goal of making code easier to read.

##parse-double

This wraps java.lang.Double/parseDouble

##parse-int

This wraps java.lang.Integer/parseInt

# Closing thoughts

There are three more methods, str-join, chop, & chomp that were already in str-utils. I changed the implementation of the methods, but the behavior should be the same.

There is a big catch with my proposed change. The signature of re-split, re-partition, re-gsub and re-sub changes. They will not be backwards compatible, and will break code. However, I think the flexibility is worth it.

# TO-DOs

There are a few more things I'd like to add, but that could done at a later date.

* Add more inflectors

The following additions become pretty easy if the propsed re-gsub is included:

* Add HTML-escape function (like Rails' h method)
* Add Javascript-escape function (like Rails' escape_javascript method)
* Add SQL-escape function

Okay, that's everything I can think of for now. I'd like to thank Stuart Sierra, and all of the contributors to this library.
This is possible because I'm standing on their shoulders.