(ns lib.sfd.swing.messages
  (:import [javax.swing JOptionPane JFileChooser]))

(defn messenger-factory
  [message-type]
  (fn [m t] (JOptionPane/showMessageDialog nil m t message-type nil)))

(defn plain-message
  "Creates a dialog that shows a plain message.  Will block the thread it is in.  Returns nil."
  ([message] (plain-message message nil))
  ([message title] ((messenger-factory JOptionPane/PLAIN_MESSAGE) message title)))

(defn info-message
  "Creates a dialog that shows an informational message.  Will block the thread it is in.  Returns nil."
  ([message] (info-message message nil))
  ([message title] ((messenger-factory JOptionPane/INFORMATION_MESSAGE) message title)))

(defn warning-message
  "Creates a dialog that shows a warning message.  Will block the thread it is in.  Returns nil."
  ([message] (warning-message message nil))
  ([message title] ((messenger-factory JOptionPane/WARNING_MESSAGE) message title)))

(defn error-message
  "Creates a dialog that shows an error message.  Will block the thread it is in.  Returns nil."
  ([message] (error-message message nil))
  ([message title] ((messenger-factory JOptionPane/ERROR_MESSAGE) message title)))

(defn question-message
  "Creates a dialog that shows a question message.  Will block the thread it is in.  Returns nil."
  ([message] (question-message message nil))
  ([message title] ((messenger-factory JOptionPane/QUESTION_MESSAGE) message title)))

;A map to turn integers into keywords
(def yes-no-decoder {JOptionPane/YES_OPTION :yes
		     JOptionPane/NO_OPTION :no})
		     
(defn yes-no-dialog
  "Creates an yes/no dialog.  Will block the thread it is in.  Returns :yes or :no"
  ([message] (yes-no-dialog message nil))
  ([message title] (yes-no-decoder 
	  (JOptionPane/showConfirmDialog nil message title JOptionPane/YES_NO_OPTION))))

(defn yes-no-cancel-dialog
  "Creates an yes/no/cancel dialog.  Will block the thread it is in.  Returns :yes or :no, or nil if cancelled"
  ([message] (yes-no-cancel-dialog message nil))
  ([message title] (yes-no-decoder
	  (JOptionPane/showConfirmDialog nil message title JOptionPane/YES_NO_CANCEL_OPTION))))

(defn input-dialog
  "Creates an input dialog.  Will block the thread it is in.  Returns the value entered as a String, nil if cancelled."
  [message] (JOptionPane/showInputDialog message))

(defn dropdown-dialog
  "Creates a dropdown dialog.  Will block the thread it is in.  Returns the value selected, or nil if cancelled."
  ([message title coll] (dropdown-dialog message title coll nil))
  ([message title coll default] (JOptionPane/showInputDialog nil message title JOptionPane/QUESTION_MESSAGE nil (into-array coll) default)))

;(defn file-open-dialog
;  ([]))

;(defn file-save-dialog
;  ([]))