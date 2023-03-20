# Hangman V2.0

## Description
For this assignment, you must develop a client-server distributed application in
Java for a phrase guessing game using RMI. The client connects to the server and specifies
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

## How to run
1. `cd src && javac *.java`
2. `rmiregistry &`
3. `java AccountServer`
4. `java WordServer`
5. `java GameServer`
6. `java Client <server address>`

## How to play
* `login/register <username> <password> or exit`
* `start <number of letters> <number of words>`
* `# - Ends the game`
* `! - Restarts the game`
* `? <word>- Checks if a word exists in the repository`
* `+ <word>- Adds word to the repository`
* `- <word>- Removes word from the repository`
* `$ - Retrieves a users score`

## Communication Design Document
### Service Descriptions:
**AccountServer**: Handles user login, registration, and scores.

**WordServer**: Word repository for the game, allowing the user to add, remove, and check if a word exists in the repository.

**GameServer**: Handles the game logic and communicates with the AccountServer and WordServer.

**Client**: Client facing interface, allowing for communication with the GameServer.

### Component Diagram
![protocol](https://user-images.githubusercontent.com/69999501/223893650-28d4de48-014b-4d4c-844d-eaff8852b5a9.png)

### Direction of method invocations
#### Client - Server
The client will invoke the following methods on the GameServer:
* `login` - login if the user has an active account
* `register` - register a new user account
* `startGame` - start a new game
* `guessLetter` - guess a letter in the phrase
* `guessPhrase` - guess the entire phrase
* `addWord` - add word to the word repository
* `removeWord` - remove word from the word repository
* `checkWord` - check if word exists in word repository
* `getScore` - get current highscore
* `restartGame` - start a new game
* `endGame` - end current game

The GameServer will then invoke the coresponding methods on either, `AccountService`, or `WordSerivce`

#### Server - Client
The server will invoke the `ping` method from the client every 2 seconds to ensure that the client is still connected, heartbeat mechanism. If the client has disconnected, the username associated with that client is removed from the system allowing user to login again as the system only permits for one instnace of the user to be logged in at a time, no simultaneous sessions from one user are allowed.

### Methods and Data exchange
#### Client - Server:

##### AccountService
* `writeToFile(username:String, password:String, score:String): boolean`
  * Used when user registration is required
  * Receives - username, password, score
  * Returns - boolean to represent success or failure of registration
* `readFromFile(username:String, password:String): String`
  * Used when user login is required
  * Receives - username and password
  * Returns - String `!noaccount!` if not username and password do not match
* `updateScore(username:String): void`
  * Updates user score by adding 100 to the current score
  * Receives - username
  
##### WordService
* `getPhrase(length:int): String`
  * Receives - number of words to generate for a phrase
  * Returns - a phrase that contains `length` words
* `addWord(word:String): boolean`
  * Syncronized method
  * Receives - word to add to repository
  * Returns - boolean based on success or faliure of adding word
* `removeWord(word:String): boolean`
  * Syncronized method
  * Receives - word to remove
  * Returns - boolean based on success or failure of removing word
* `checkWord(word:String): boolean`
  * Syncronized method
  * Receives - word to check
  * Returns - boolean based on success or failure of existance of word
  
##### GameHandlerService
* `startGame(player:String, number_of_words:int, failed_attempt_factor:int): String`
  * Receives - player username, number of words they want in phrase, and how many attempts
  * Returns - phrase that has been converted into dashes (-) + attempt counter
* `guessLetter(player:String, letter:char): String`
  * Receives - player username and guessed letter
  * Returns - letter in the correct place in the phrase + updated attempt counter
* `guessPhrase(player:String, phrase:String): String`
  * Receives - player username and guessed phrase
  * Returns - correct/incorrect guess message + updated attempt counter
* `endGame(player:String): String`
  * Removes player from `gameState`
  * Receives - player username
  * Returns - game ended message + next steps
* `restartGame(player:String): String`
  * Removes player from `gameState`
  * Receives - player username
  * Returns - game restart message
* `addWord(word:String): boolean`
  * Invokes `addWord` method in `WordService`
  * Receives - word to add to repository
  * Returns - boolean based on success or faliure of adding word
* `removeWord(word:String): boolean`
  * Invokes `removeWord` method in `WordService`
  * Receives - word to remove from repository
  * Returns - boolean based on success or faliure of removing word
* `checkWord(word:String): boolean`
  * Invokes `checkWord` method in `WordService`
  * Receives - word to check for in repository
  * Returns - boolean based on success or faliure of finding word
* `login(username:String, password:String): int`
  * Invokes `readFromFile` method in `AccountService`
  * Receives - username and password
  * Returns - integer representing whether user can be logged in or not
* `register(username:String, password:String): String`
  * Invokes `writeToFile` method in`AccountService`
  * Receives - username and password
  * Returns - same message as `writeToFile`
* `logOut(String:username): void`
  * Removes player username from loggedIn users
  * Receives - username
* `getScore(username:String, password:String): String`
  * Receives - username and password
  * Returns - user's highscore
* `addClientListener(client:ClientListener, username:String): void`
  * Registers client listener for heartbeat mechanism

#### Server - Client: 

##### AccountService : None
##### WordService: None
##### GameHandlerService:
* `ping(): void`
  * ping client for hearbeat checks
##### ClientListener:
* `ping(): void`
  * Recieve ping from server for hearbeat checks

### Design choices
* `login()` returning `int` instead of `boolean`:
An integer is being returned to allow us to determine if a user should be logged in or not. This allows us to ensure one session per user, as well catching any potential errors, and showing meaningful messages to the user
  1. No account - return 1
  2. Already logged in - return 2
  3. Game state is null - return 3
  4. Other error - return 4
* `ping()` - This method is a heartbeat that will periodically check if the client is still connected. It is important to have this because we are keeping track of usernames, and only allowing them to have one active session, meaning that if a `RemoteException` is thrown by any of the method calls and the client crashes, their username would not be removed from the `loggedInUsers` without this mechanism. This will be a problem as the user will not be able to login unless the `GameServer` is restarted. This method will prevent such event from occurring.
