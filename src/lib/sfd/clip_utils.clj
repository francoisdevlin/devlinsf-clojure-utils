(ns ^#{:doc "This is a collection of utilities I use to
interact with the clipboard.  Very, very useful for data
munging."}
  lib.sfd.clip-utils)

(defn- get-sys-clip
  "A helper fn to get the clipboard object"
  []
  (. (java.awt.Toolkit/getDefaultToolkit) getSystemClipboard))

(defn get-clip
  "Get the contents of the clipboard.  Currently only supports text."
  []
  (let [clipboard (get-sys-clip)]
    (if clipboard
      (let [contents (. clipboard getContents nil)]
	(cond 
	 (nil? contents) nil
	 (not (. contents isDataFlavorSupported java.awt.datatransfer.DataFlavor/stringFlavor)) nil
	 true (. contents getTransferData java.awt.datatransfer.DataFlavor/stringFlavor))))))

(defn set-clip!
  "Set the contents of the clipboard.  Currently only supports text."
  [input-string]
  (if input-string
    (let [clipboard (get-sys-clip)]
      (if clipboard
	(do
	  (let [selection (java.awt.datatransfer.StringSelection. input-string)]
	    (. clipboard setContents selection nil))
	  input-string)))))

(defn read-clip
  "Used to read an s-expression in the clipboard."
  []
  (read-string (get-clip)))

(defn eval-clip
  "Used to evaluate an s-expression in the clipboard."
  []
  (eval (read-clip)))
