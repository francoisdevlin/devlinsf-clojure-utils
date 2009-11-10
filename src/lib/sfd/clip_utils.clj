(ns lib.sfd.clip-utils)
  ;(:import (java.awt.datatransfer Clipboard 
;				  ClipboardOwner
;				  Transferable
;				  StringSelection
;				  DataFlavor
;				  UnsupportedFlavorException)
;	   java.awt.Toolkit))

;java.awt.datatransfer.

(defn get-sys-clip
  []
  (. (java.awt.Toolkit/getDefaultToolkit) getSystemClipboard))

(defn get-clip
  []
  (let [clipboard (get-sys-clip)]
    (if clipboard
      (let [contents (. clipboard getContents nil)]
	(cond 
	 (nil? contents) nil
	 (not (. contents isDataFlavorSupported java.awt.datatransfer.DataFlavor/stringFlavor)) nil
	 true (. contents getTransferData java.awt.datatransfer.DataFlavor/stringFlavor))))))

(defn set-clip!
  [input-string]
  (if input-string
    (let [clipboard (get-sys-clip)]
      (if clipboard
	(do
	  (let [selection (java.awt.datatransfer.StringSelection. input-string)]
	    (. clipboard setContents selection nil))
	  input-string)))))

(defmacro defclip
  [symbol-name]
  `(def ~symbol-name (get-clip)))

(defn read-clip
  []
  (read-string (get-clip)))

(defn eval-clip
  []
  (eval (read-clip)))

(+ 2 2)
