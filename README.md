# Vert.x Example Maven Project

TODO - these are the things that I still need to get done:

Should not be able to increment a mapped signal
Should not be able to define a signal that's already been defined
spaces for combine with add
when a new signal is create is needs to get the initial value of all dependent signals
move all addresses to a single class to avoid typing errors
Need a way to enable/disable glitch avoidance
support simple arithmetic operations like y=x+1
variables regex should not match numbers
Need to think about having multiple windows so we can have a distributed application

---- Depdencency graph analysis to avoid glitches


alexa = 3
brian = alexa
dog = brian - alexa

     alexa = 4 
      /     \
  brian=4   dog=brian=alexa=3-4=-1

