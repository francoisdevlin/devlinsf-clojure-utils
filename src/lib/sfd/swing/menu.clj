(ns lib.sfd.swing.menu
  (:import (javax.swing JMenu JMenuItem JMenuBar JSeparator JPopupMenu KeyStroke))
  (:require [lib.sfd.swing [events :as events]]))

(defn menu-bar 
  [& menus]
  (let [a-menu-bar (JMenuBar. )]
    (do
      (doseq [a-menu menus]
	(.add a-menu-bar a-menu))
      a-menu-bar)))

(defn menu
  "Currently does not support accelerators"
  [name & items]
  (let [a-menu (JMenu. name)]
    (do
      (doseq [an-item items]
	(.add a-menu an-item))
      a-menu)))

(defn menu-item
  [name action]
  (events/add-action-listener 
   (JMenuItem. name)
   (events/action-listener action)))

(defn menu-sep
  []
  (JSeparator. ))

(defn popup-menu
  [& items]
  (let [a-menu (JPopupMenu. )]
    (do
      (doseq [an-item items]
	(.add a-menu an-item))
      a-menu)))

(defn accel
  "Sets the accellerator for a menu item.  Returns the menu item."
  ([m vk] (accel m vk 0))
  ([m vk mods]
     (doto m
       (.setAccelerator  (KeyStroke/getKeyStroke vk mods)))))