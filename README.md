# Hangman V1.0

## How to run
 1. `javac AccountService.java && java AccountService`
 2. `javac WordService.java && java WordService`
 3. `javac Server.java && java Server [port]`
 4. `javac Client.java && java Client [port]`
 
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
phrase correctly. The server wins over when the counter of allowed failed attempts reaches zero. The server should compute the total score of games using
a score counter: if the client wins the score counter is incremented, if the client
loses the score counter is decremented.
The server should keep track of client game history so returning clients are able
to see their previous score, and new rounds of games update client score.
The server upon initialization builds a repository of words based on a standard
Unix words file. The server must allow the client to add words to the repository,
or remove a words from the repository, or check if the repository already contains
a given word.

## Protocol Design Document
### Service Descriptions:
**AccountService**: Multithreaded TCP server, allowing up to 10 connections via ThreadPool, that handles user login and registration. It also keeps track of user scores.

**WordService**: UDP server that acts as a word repository for the game, allowing the server to add, remove, and check if a word exists in the repository.

**Server**: Multithreaded TCP server, allowing up to 10 connections via ThreadPool, that handles the game logic and communicates with the AccountService and WordService.

**Client**: TCP client that handles user input and communicates with the Server.

### Sequence Diagram
![Sequence Diagram](https://user-images.githubusercontent.com/69999501/217333895-4300829f-7d03-43ae-a306-891d0c7ea776.png)



### Communication Protocol Details
#### Client - Server:

Client will establish a connection with the server over TCP. The client will send a message to the server with the following format:

`[login/register] [username] [password]` OR `exit`

Once the client has logged in, or registered, the client will send a message to the server with the following format:

`start <i> <f>` - Where i is the number of phrases a user wants and f is the number of failed attempts a user wants.

At any point the client can send a message to the server with the following format:

`? <word>` - Where word is the word the user wants to check if it exists in the collection.
`$` - Check their score.
`#` - Exit the game.

Once the client starts a game, they will also have access to the following command:
    
`!` - Where they can end the current game and start a new one.

Upon guessing the correct phrase the client will receive a message from the server saying "You win!". The server will then send a message to AccountService with the following format:

`get <username>` - More details are mentioned under Server - AccountService.

Upon receiving the reply, the server will send a message to AccountService with the following format updating the user score:

`post <username> <score>` - More details are mentioned under Server - AccountService.

If user enters `exit`, **before logging in**, the connection will be terminated.

If user enters an invalid command, the server will send a message to the client saying "Invalid command" and re-prompt user.

#### Server - AccountService:

The server will establish a connection with the AccountService over TCP. The server will send a message to the AccountService with the following format:

`[login/register] [username] [password]`

AccountService will either reply with `!success!` or `!fail` if register is not successful OR `!noaccount` if login is not successful, if login is successful it will return the username followed by the users' score.

##### Updating Score:
Upon receiving `get` message from the server, AccountService will reply with the username followed by the users' score.

Upon receiving `post` message from the server, AccountService will update the users' score.

#### Server - WordService:

The server will establish a connection with the WordService over UDP. The server will send a message to the WordService with one of the following formats depending on the client request:

`start <i>` - Where i is the number of phrases a user wants. The server will then receive a list of phrases from the WordService.

`? <word>` - Where word is the word the user wants to check if it exists in the collection. The server will then receive a `true` or `false` from the WordService.




