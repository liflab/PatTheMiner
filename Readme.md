A data mining and trend extraction palette for BeepBeep 3
=========================================================

This project is an extension to the [BeepBeep
3](https://liflab.github.io/beepbeep-3), event stream processing engine,
called a *palette*, that provides functionalities to compute various
statistical and basic clustering tasks on streams of events.

What is this?
-------------

Please refer to the following research paper for detailed information and
examples of what this extension can do.

> M. Roudjane, D. Rebaïne, R. Khoury, S. Hallé. (2018). *Real-Time Data Mining
> for Event Streams*. Proc. EDOC 2018. DOI: 10.1109/EDOC.2018.00025.
> [ResearchGate](https://www.researchgate.net/publication/328172038)

Further documentation might be added to this Readme some time in the future
(any help is welcome!).

Building this palette
---------------------

To compile the palette, make sure you have the following:

- The Java Development Kit (JDK) to compile. The palette complies
  with Java version 6; it is probably safe to use any later version.
- [Ant](http://ant.apache.org) to automate the compilation and build process

The palette also requires the following Java libraries:

- The latest version of [BeepBeep 3](https://liflab.github.io/beepbeep-3)
- The latest version of
  [Apache Commons Math 3](http://commons.apache.org/proper/commons-math)
- Version 3.6.x of [Weka](https://www.cs.waikato.ac.nz/ml/weka/index.html)
  (not the latest version, so that Java 1.6 is supported)

These three dependencies can be automatically downloaded and placed in the
`dep` folder of the project by typing:

    ant download-deps

From the project's root folder, the sources can then be compiled by simply
typing:

    ant

This will produce a file called `pattheminer.jar` in the folder. This file
is *not* runnable and stand-alone. It is meant to be used in a Java project
alongside `beepbeep-3.jar`, `commons-math3-x.x.x.jar` and `weka.jar` (the JAR
files downloaded by the Ant build script).

<!-- :maxLineLen=78: -->
