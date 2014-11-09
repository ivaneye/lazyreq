(defcfg cfg
        :mappings [{:url           "/xmlrpc"
                   :method        "POST"
                   :clojure-pres  ["lazyreq.pres.resolve-body" "lazyreq.pres.format-header" "lazyreq.pres.next-url"]
                   :clojure-errs  ["lazyreq.errs.xmlrpc-err"]
                   :clojure-posts ["lazyreq.posts.ungzip" "lazyreq.posts.save-resp"]
                   :core          "lazyreq.cores.xmlrpc"}
                   {:url           "/xmlrpc2"
                    :method        "POST"
                    :clojure-pres  ["lazyreq.pres.resolve-body" "lazyreq.pres.format-header" "lazyreq.pres.next-url"]
                    :clojure-errs  ["lazyreq.errs.xmlrpc-err"]
                    :clojure-posts ["lazyreq.posts.ungzip" "lazyreq.posts.save-resp"]
                    :core          "lazyreq.cores.xmlrpc"}
                   ]
        :tasks ["lazyreq.task.recall"])
