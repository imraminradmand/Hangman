# Phrase Guessing Game

## Description
For this assignment, you must develop a client-server distributed application in
Java for a phrase guessing game. The client connects to the server and specifies
the game level they want to play. The server then chooses a number of words
from a dictionary based on the game level, and asks the client to guess the
phrase. The client (the player) tries to guess the words chosen by the server
by suggesting letters (one letter at a time) or the whole phrase. If the client
suggests a letter that occurs on the phrase, the server places the letter in all
its positions; otherwise the counter of allowed failed attempts is decremented.
At any time the client is allowed to guess the whole phrase. A failed attempt
occurs either when a suggested letter does not appear in the phrase, or when
the suggested whole phrase does not match.
The client wins when the client completes the phrase, or guesses the whole
phrase correctly. The server wins over when the counter of allowed failed at-
tempts reaches zero. The server should compute the total score of games using
a score counter: if the client wins the score counter is incremented, if the client
loses the score counter is decremented.
The server should keep track of client game history so returning clients are able
to see their previous score, and new rounds of games update client score.
The server upon initialization builds a repository of words based on a standard
Unix words file. The server must allow the client to add words to the repository,
or remove a words from the repository, or check if the repository already contains
a given word.

## Implementation Details

