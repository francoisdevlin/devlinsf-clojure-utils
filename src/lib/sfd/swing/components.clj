(ns lib.sfd.swing.components
  (:import (javax.swing JButton JLabel JList JPanel 
			JTextField JComponent JTabbedPane
			JEditorPane JFileChooser))
  (:require [lib.sfd.swing [events :as events]]))

(defn jbutton
  "Creates a JButton and attaches an action to it."
  [text action]
  (events/add-action-listener 
   (JButton. text)
   (events/action-listener action)))

(defn jlabel
  "Creates a JLabel"
  [text]
  (JLabel. text))

(defn jlist
  [coll]
  (JList. (into-array coll)))

(defn jpanel
  [& comps]
  (let [panel (JPanel. )]
    (do
      (doseq [a-comp comps]
	(.add panel a-comp))
      panel)))

(defn jtabbedpane
  ([comps] (jtabbedpane comps (cycle [""])))
  ([comps titles] (jtabbedpane comps titles (cycle [nil])))
  ([comps titles icons] (jtabbedpane comps titles icons (cycle [""])))
  ([comps titles icons tooltips]
     (let [tabs (JTabbedPane. )
	   inputs (partition 4 (interleave comps titles icons tooltips))]
       (do
	 (doseq [data inputs]
	   (let [a-comp (nth data 0)
		 title (nth data 1)
		 icon (nth data 2)
		 tt (nth data 3)]
	     (.addTab tabs title icon a-comp tt)))
	 tabs))))

(defn jtextfield
  ([] (JTextField. ))
  ([default] (JTextField. default)))

(defn jeditorpane
  ([] (JEditorPane. )))

(defn tool-tip
  [#^JComponent component text]
  (doto component
    (.setToolTipText text)))