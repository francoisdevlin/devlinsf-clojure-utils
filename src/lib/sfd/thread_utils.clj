(ns lib.sfd.thread-utils)

(defn sleep 
  "A simple wrapper for Thread/sleep"
  [ms]
  (Thread/sleep ms))

(defn daemon
  "Creates a new daemon thread and sets runnable to f"
  [f]
  (doto (Thread. f) 
    (.setDaemon true)
    (.start)))

(defn thread
  "Creates a new thread and sets runnable to f"
  [f]
  (doto (Thread. f) 
    (.start)))

(defn periodic
  [f ms]
  (fn []
    (loop [] (f) (sleep ms) (recur))))