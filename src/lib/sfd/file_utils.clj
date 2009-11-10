(ns lib.sfd.file-utils
  (:import java.io.File))

(def *fs* File/separator)
(def *ps* File/pathSeparator)

(defmulti to-file class)

(defmethod to-file String
  [s]
  (File. s))
(defmethod to-file File
  [f]
  f)

;;File Navigation
(defn filecat
  [& args]
  (apply str (interpose *fs* args)))

(defn filepath
  [f]
  (.getAbsolutePath (to-file f)))

(defn filename
  [f]
  (.getName (to-file f)))

(defn pwd
  [f]
  (.getParent (to-file f)))

(defn ls
  [f]
  (seq (.list (to-file f))))

;;File Attributes
(defn directory?
  [f]
  (.isDirectory (to-file f)))

(defn exists?
  [f]
  (.exists (to-file f)))

(defn file?
  [f]
  (.isFile (to-file f)))

(defn hidden?
  [f]
  (.isHidden (to-file f)))

(defn read?
  [f]
  (.canRead (to-file f)))

(defn write?
  [f]
  (.canWrite (to-file f)))

(defn last-modified
  [f]
  (.lastModified (to-file f)))

;;File Modifications
(defn touch
  [f]
  (.createNewFile (to-file f)))

(defn mkdir
  "This wraps the java mkdirs method, in order to immitate the unix mkdir (I think)"
  [f]
  (.mkdirs (to-file f)))

(defn mv
  [source dest]
  (.renameTo (to-file source) (to-file dest)))

(defn rm
  [f]
  (.delete (to-file f)))