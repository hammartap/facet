# facet

A Clojure library designed to use Remote Camera Control API Beta by Sony Corporation.  
[Remote Camera Control API Beta](https://developer.sony.com/develop/cameras/)

Further example, such a demo app which display retrieved liveview images is below:
* [facet-liveview](https://github.com/hammartap/facet-liveview)


## Usage
`project.clj` file.
```clojure
:dependencies [[facet "0.2.0"]]
```

In `REPL`, 
```clojure
> (ns your.favorite.namespace)
> (use 'facet.core)

;; Start device discovery.
> (init)

;; Then, your camera information is set to SonyCamInfo.
> SonyCamInfo

;; Take a picture.
> (takeAndFetchPicture)
;; This returns a map like below.
;; {:id 1, :result "http://10.0.0.1:60152/pict130104_0510450000.JPG?%211234%21http%2dget%3a%2a%3aimage%2fjpeg%3a%2a%21%21%21%21%21"}
;; So you can now grab your photo by accessing to :result URL.

;; You can retrieve liveview image using code below:
;; First, define an agent which hold current liveview image.
> (def foo-agent (agent nil))
;; Define a closure which function stops liveview retrieving.
> (def foo-stopper (retrieveLiveView foo-agent))

;; Then, you can save current liveview image:
> (ImageIO/write (ImageIO/read (ByteArrayInputStream. @foo-agent)) "jpg" (File. "foo.jpg"))
;; Each time, this will save current liveview image.
> (ImageIO/write (ImageIO/read (ByteArrayInputStream. @foo-agent)) "jpg" (File. "foo.jpg"))
> (ImageIO/write (ImageIO/read (ByteArrayInputStream. @foo-agent)) "jpg" (File. "foo.jpg"))

;; At last, stop the image-retrieving-thread.
> (foo-stopper true)
```


## License

Copyright © 2014 hammartap

Distributed under the Eclipse Public License, the same as Clojure.
