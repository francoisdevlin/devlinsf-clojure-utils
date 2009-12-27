(ns lib.sfd.swing.buttons
  (:import (javax.swing JButton))
  (:require [lib.sfd.swing [events :as events]]))

(defn button
  [label action] (events/add-action-listener 
		  (button label)
		  (events/action-listener action)))