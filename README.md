Drega - Distributed Reactive Environment with Glitch Avoidance
==============================================================
Drega is a simple programming environment that allows you to make simple
reactive signals.  Signals can depend on the values of other signals and
when updates occur events are sent out to keep other signals up to date.
Events are processed using a glitch avoidance algorithm so invariants 
established when signals are created are not violated.

Installation
------------
This project is built on top of [Vert.x](http://vertx.io/).  To run:

* Install the 2.1M1 version of vertx and put the vertx executable on your
  path
* Download this repository and compile using `mvn install`
* cd into the target directory and run `vertx runmod org.bcard~drega~0.1`
* Type `help` to get a list of available commands
