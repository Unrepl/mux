(ns unrepl.mux
  (:require [clojure.edn :as edn]))

(defn mux
  ([] (mux clojure.core.server/repl))
  ([accept] (mux accept (* 16 1024)))
  ([accept pipe-size]
    (let [out *out*
          tagging-writer
          (fn [tag]
            (proxy [java.io.Writer] []
              (close [] (locking out
                          (binding [*out* out]
                            (prn [tag nil]))))
              (flush [] (locking out
                          (binding [*out* out]
                            (flush))))
              (write
                ([x]
                  (locking out
                    (binding [*out* out]
                      (prn [tag (cond 
                                  (string? x) x
                                  (integer? x) (str (char x))
                                  :else (String. ^chars x))]))))
                ([string-or-chars off len]
                  (when (pos? len)
                    (locking out
                      (binding [*out* out]
                        (prn [tag (subs (if (string? string-or-chars) string-or-chars (String. ^chars string-or-chars))
                                    off (+ off len))]))))))))
          spawn-thread (fn [tag]
                         (let [pipe-in (java.io.PipedWriter.)
                               pipe-out (-> (java.io.PipedReader. pipe-in pipe-size)
                                          clojure.lang.LineNumberingPushbackReader.)]
                           (-> (fn [] 
                                 (binding [*in* pipe-out
                                           *out* (tagging-writer tag)]
                                   (accept)))
                             Thread. .start)
                           pipe-in))]
      (loop [channels {}]
        (when-some [[tag ^String content] (clojure.edn/read)]
          (recur
            (loop [channels channels]
             (if-some [^java.io.Writer out (channels tag)]
               (if content
                 (do (.write out content) channels)
                 (do (.close out) (dissoc channels tag)))
               (recur (assoc channels tag (spawn-thread tag)))))))))))