# Vert.x Example Maven Project

TODO - these are the things that I still need to get done:

Should not be able to increment a mapped signal
Should not be able to define a signal that's already been defined
spaces for combine with add
when a new signal is create is needs to get the initial value of all dependent signals
move all addresses to a single class to avoid typing errors
Need a way to enable/disable glitch avoidance
support simple arithmetic operations like y=x+1
Need to think about having multiple windows so we can have a distributed application
Should probably add a .get address for making a signal broadcast it's current value
Console is too quick, going over some of the signal output.  Is it waiting for commands to complete?


DONE Depdencency graph analysis to avoid glitches
DONE Numbering events, preforming dependency analysis on events