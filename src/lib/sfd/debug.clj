(ns lib.sfd.debug
  (:import [javax.swing JFrame JTree
	    JTextArea JTextField JButton
	    JSplitPane JScrollPane
	    KeyStroke ImageIcon]
	   [javax.swing.tree TreeSelectionModel DefaultTreeCellRenderer]
	   [java.awt.event KeyEvent InputEvent])
  (:use clojure.inspector
	clojure.walk
	lib.sfd.same
	lib.sfd.str-utils
	lib.sfd.swing.menu
	lib.sfd.swing.messages
	lib.sfd.clip-utils))

(def ctrl-key
     (if (= ((into {} (System/getProperties)) "os.name")
	    "Mac OS X")
       InputEvent/META_DOWN_MASK
       InputEvent/CTRL_DOWN_MASK))
       

(defn macroexpand-all-sfd
  "Recursively performs all possible macroexpansions in form."
  [form]
  (prewalk (fn [x] (if (seq? x) (same macroexpand x) x)) form))

(defn safe-meta
  "Gets the metadata of a symbol without throwing an excpetion.  Returns
an empty map if the symbol doesn't exist."
  [s] 
  (if (and (symbol? s) 
           (resolve s))
    (eval `(meta (var ~s)))
    {}))

(def parent-frame nil)

(def tree-renderer
     (let [default-renderer (DefaultTreeCellRenderer. )
	   default-leaf (.getLeafIcon default-renderer)]
       (proxy [DefaultTreeCellRenderer] []
	   (getTreeCellRendererComponent
	    [tree value selected? expanded? leaf? row has-focus?]
	    (do
	      (if (fn? value)
		(.setLeafIcon default-leaf)
		(.setLeafIcon nil))
	      (.getTreeCellRendererComponent
	       default-renderer
	       tree value selected? expanded? leaf? row has-focus?)
	      )))))
	      

(defn display-doc
  [s]
  (let [m (safe-meta s)
	arglists (:arglists m)
	docstring (if (:doc m)
		    (:doc m)
		    "No Docstring Available")
	text (doto (JTextArea.
		    (str s "\nArgs:\n" (str-join "\n" (map str arglists)) "\n\n  "
			 (str-join " " (map trim (split-lines docstring)))))
	       (.setLineWrap true)
	       (.setEditable false))
	scroll (JScrollPane. text)]
    (doto (JFrame. (str "Help for " s))
      (.add scroll)
      (.setVisible true)
      (.setSize 300 200))))

(defn get-selected-node
  "A specialized fn to get the first selected item in a tree.  Works
well with a SINGLE_TREE_SELECTION model."
  [tree]
  (first (map #(.getLastPathComponent %)
	      (.getSelectionPaths tree))))

(defn make-doc-menu-item
  [tree]
  (accel (menu-item "Doc"
		    (fn [evt]
		      (display-doc (get-selected-node tree)))) (KeyEvent/VK_F1)))

(defn eval-node
  [tree]
  (eval (get-selected-node tree)))

(defn realize-lazy-seq
  [tree]
  (do
    (doall (take 10 (eval-node tree)))))
    


(defn eval-node-menu-item
  [tree]
  (accel (menu-item "Eval"
		    (fn [evt]
		      (try (plain-message (eval-node tree))
			   (catch Exception e (error-message (str e))))))
	 KeyEvent/VK_F5))

(defn realize-seq-menu-item
  [tree]
  (accel (menu-item "Realize Seq"
		    (fn [evt]
		      (try (plain-message (str-join " " (realize-lazy-seq tree)))
			   (catch Exception e (error-message (str e))))))
	 KeyEvent/VK_F5 InputEvent/SHIFT_DOWN_MASK))
  

(defn deep-inspect
  "Creates a graphical (Swing) inspector on the supplied hierarchical data"
  [data]
  (let [expr (agent data)
	tree (JTree. (tree-model @expr))
	make-macro-menu (fn [title f vk]
			  (accel (menu-item title
					    (fn [evt] (.setModel tree (tree-model (f @expr)))))
				 vk ctrl-key))
	mb (menu-bar
	    (menu "Edit"
		  (menu-item "Copy"
			     (fn [evt] (set-clip! (str @expr))))
		  (menu-item "Paste"
			     (fn [evt] (send expr (fn [a] (let [new-data (read-clip)]
							    (.setModel tree (tree-model new-data))
							    new-data))))))
	    (menu "Exp. Eval"
		  (eval-node-menu-item tree)
		  (realize-seq-menu-item tree))
	    (menu "Macros"
		  (make-macro-menu "None" identity KeyEvent/VK_1)
		  (make-macro-menu "Expand" macroexpand KeyEvent/VK_2)
		  (make-macro-menu "Expand 1" macroexpand-1 KeyEvent/VK_3)
		  (make-macro-menu "Expand All" macroexpand-all-sfd KeyEvent/VK_4))
	    (menu "Help"
		  (make-doc-menu-item tree)))]
    (do (-> tree .getSelectionModel (.setSelectionMode TreeSelectionModel/SINGLE_TREE_SELECTION))
	(doto tree
	  (.setComponentPopupMenu (popup-menu (make-doc-menu-item tree)
					      (eval-node-menu-item tree)
					      (realize-seq-menu-item tree)
					      (menu-item "Sub Inspector"
							 (fn [evt]
							   (deep-inspect (get-selected-node tree))))))
	  ;(.setCellRenderer tree-renderer)
	  )
	(doto (JFrame. "Clojure Inspector")
	  (.add (JScrollPane. tree))
	  (.setJMenuBar mb)
	  (.setSize 400 400)
	  (.setVisible true)))))