# Vert.x Example Maven Project

TODO - these are the things that I still need to get done:

move all addresses to a single class to avoid typing errors
support simple arithmetic operations like y=x+1
Should not be able to define a signal that's already been defined

DONE Depdencency graph analysis to avoid glitches
DONE Numbering events, preforming dependency analysis on events

Explanation of error:
6 -> 1 -> 3 (stop, 3 waits for 6 -> 5)
6 -> 1 -> 2 (2 waits for 7)
7 -> 2 (2 all set, generates next message)
7 -> 2 -> 4 (4 waits for update from 3)
6 -> 5 -> 3 (3 all set, generates next message)
6 -> 5 -> 3 -> 4 (4 needs values for 1 and 6)

4 has two messages:
7 -> 2 -> 4
6 -> 5 -> 3 -> 6
needs:
6 -> 1 -> 3 and
6 -> 1 -> 2 

When glitch resolved, need to forward all signal info for dependencies, not just the signal info contained in message.