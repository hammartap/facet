# facet

A Clojure library designed to use Remote Camera Control API Beta by Sony Corporation.
[Remote Camera Control API Beta](https://developer.sony.com/develop/cameras/)


## Usage
;; Namespace setting and load files.
The `project.clj` file.
```clj
:dependencies [[facet "0.1.0-SNAPSHOT"]]
```

In REPL, 
```clj
(ns facet.core)
(use 'facet.core)

;; Start device discovery.
(init)

;; Then, your camera information is updated.
SonyCamInfo

;; Take a picture.
(actTakePicture)
;; Result return like below.
;; #<JSONObject {"id":1,"result":[["http://10.0.0.1:60152/pict130104_0510450000.JPG?%211234%21http%2dget%3a%2a%3aimage%2fjpeg%3a%2a%21%21%21%21%21"]]}>
;; You can now grab your photo by accessing to "result" URL.
```


## License

Copyright © 2014 hammartap

Distributed under the Eclipse Public License, the same as Clojure.
