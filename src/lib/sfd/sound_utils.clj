(ns lib.sfd.sound-utils
  (:import (javax.sound.sampled AudioFormat AudioFormat$Encoding 
				AudioFileFormat$Type AudioInputStream AudioSystem
				Clip DataLine$Info)
	   (java.io File FileInputStream)))

(def file-type
     {:aifc AudioFileFormat$Type/AIFC
      :aiff AudioFileFormat$Type/AIFF
      :au   AudioFileFormat$Type/AU
      :snd  AudioFileFormat$Type/SND
      :wave AudioFileFormat$Type/WAVE})

(def encoding
     {:alaw AudioFormat$Encoding/ALAW
      :pcm-signed AudioFormat$Encoding/PCM_SIGNED
      :pcm-unsigned AudioFormat$Encoding/PCM_UNSIGNED
      :ulaw AudioFormat$Encoding/ULAW})

(defn audio-stream
  [input audio-format]
  (AudioInputStream. (FileInputStream. input) audio-format (.length input)))

(defn data-line-clip
  [audio-format]
  (DataLine$Info. Clip audio-format))

(defn play-file
  [input]
  (let [input-file (File. input)
	audio-format (.getFormat (AudioSystem/getAudioFileFormat input-file))
	audio-in (audio-stream input-file audio-format)
	line (data-line-clip audio-format)
	clip (AudioSystem/getLine line)]
    (do
      (.open clip audio-in)
      (.start clip))))