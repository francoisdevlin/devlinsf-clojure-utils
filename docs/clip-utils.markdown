# Clipboard Utilities 

Namespace: lib.devlinsf.clip-utils

This is designed to support ad-hoc data processing and spreadsheet wrangling.  Also, it could be useful for adding custom cut and paste to your own Swing applications.

Currently only moving text objects between applications is supported.  There a few known quirks with set-clip! in OS X.  Seems to work fine on XP.  Have not tested Vista or any variants of Linux.

## Clipboard Usage 

Here's a quick rundown of how to use the methods.

### Cut & Paste 
Assume "Clojure is Awesome" is on the clipboard

* Use the get-clip function to return the data as a string.

  	user=>(get-clip)

  	"Clojure is Awesome"

  	user=>(count (get-clip))

  	18

* Use the set-clip! function to paste a string to the clipboard.

	user=>(set-clip! "Clojure is Great")

	;"Clojure is Great" is now on the clipboard

### Cut & Paste S-exps
Assume the following is in the clipboard

  (+ 2 2)

* Use the read-clip function to return the clipboard data as an S-exp (if applicable).

  	user=>(read-clip)

  	(+ 2 2)

  	user=>(count (read-clip))

  	3

* Use the eval-clip function to evaluate the S-exp in the clipboard (if applicable).

  	user=>(eval-clip)

  	4

### Storing Clippings 

* Use the defclip macro to store the clipping in a variable.

  	user=>(defclip a-symbol)

  	;a-symbol now stores the contents of the clipboard.  Great for REPL hacking.
