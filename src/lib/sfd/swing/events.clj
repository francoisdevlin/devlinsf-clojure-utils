;designed to create, add, remove & extend listeners.
(ns lib.sfd.swing.events
  (:import (java.awt.event ActionListener
			   KeyListener
			   MouseListener
			   MouseEvent)))

(defn no-action
  "Creates a no-action event.  Returns the event"
  [evt]  evt)

(defn nil-handler? [handler]
  (if handler
    handler
    no-action))

(defn action-listener 
  "Take a function that expects one argument, evt, and wraps it in an action listener proxy."
  [action]
  (proxy [ActionListener] []
      (actionPerformed [e] (action e))))

(defn extned-action-listener
  "TO DO"
  [action component])

(defn add-action-listener
  "Expects a source, such as a JButton, and an event handler, something that implements ActionListener.  Returns source."
  [source handler]
  (doto source
    (. addActionListener handler)))

(defn remove-action-listener
  "Expects a source, such as a JButton, and an event handler, something that implements ActionListener"
  [source handler]
  (doto source 
    (. removeActionListener handler)))

(defn key-listener
  "Takes three functions, typed, pressed and released.  If any value
  is set to nil, the no-action handler is used.  Please read the javadocs
  for KeyListener and KeyEvent for more information."
  [typed pressed released]
  (let [typed-handler (nil-handler? typed)
	pressed-handler (nil-handler? pressed)
	released-handler (nil-handler? released)]
    (proxy [KeyListener] []
      (keyTyped [e] (typed-handler e))
      (keyPressed [e] (pressed-handler e))
      (keyReleased [e] (released-handler e)))))

(defn add-key
  "Expects a source, such as a JButton, and an event handler, something that implements KeyListener"
  [source handler]
  (doto source
    (. addKeyListener handler)))

(defn remove-key
  "Expects a source, such as a JButton, and an event handler, something that implements KeyListener"
  [source handler]
  (doto source
    (. removeKeyListener handler)))

(defn mouse-listener
  "Blah blah blah"
  [clicked
   entered
   exited
   pressed
   released]
  (let [clicked-handler (nil-handler? clicked)
	entered-handler (nil-handler? entered)
	exited-handler (nil-handler? exited)
	pressed-handler (nil-handler? pressed)
	released-handler (nil-handler? released)
	]
    (proxy [MouseListener] []
      (mouseClicked [e] (clicked-handler e))
      (mouseEntered [e] (entered-handler e))
      (mouseExited [e] (exited-handler e))
      (mousePressed [e] (pressed-handler e))
      (mouseReleased [e] (released-handler e)))))

(defn add-mouse
  "Expects a source, such as a JButton, and an event handler, something that implements KeyListener"
  [source handler]
  (doto source
    (. addMouseListener handler)))

(defn remove-mouse
  "Expects a source, such as a JButton, and an event handler, something that implements KeyListener"
  [source handler]
  (doto source
    (. removeMouseListener handler)))

(defn expand-mouse-clicked
  "A standard cond based on the input.  Expects a map with keys :single, :double, :right, :middle"
  [handler-map]
  (let [default-handler (nil-handler? (:default handler-map))
	single-handler (nil-handler? (:single handler-map))
	double-handler (nil-handler? (:double handler-map))
	right-handler (nil-handler? (:right handler-map))
	middle-handler (nil-handler? (:middle handler-map))]
    (fn [evt]
      (cond 
	(= (. evt getButton) MouseEvent/BUTTON3) (middle-handler evt)
	(= (. evt getButton) MouseEvent/BUTTON2) (right-handler evt)
	(= (. evt getClickCount) 1) (single-handler evt)
	(= (. evt getClickCount) 2) (double-handler evt)))))

;;Needs some sort of immutable dispatch...
(defn expand-modifier
  [modifier-map]
  (let [default-handler (nil-handler? (:default modifier-map))
	shift-handler (nil-handler? (:shift modifier-map))
	control-handler (nil-handler? (:control modifier-map))
	alt-handler (nil-handler? (:alt modifier-map))]
    (fn [evt]
      (cond
	(. evt isShiftDown) (shift-handler evt)
	(. evt isControlDown) (control-handler evt)
	(. evt isAltDown) (alt-handler evt)
	true (default-handler evt)))))