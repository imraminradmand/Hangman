# Hangman V3.0

## Description
You will add failure detection and recovery mechanisms for the RMI
client. First, you need implement a basic request deduplication mechanism. You
must also make sure client records are removed if a client is suspected as crashed.
The client must send the server heart-beats via a remote method invocation.
The server must clean-up the client record in case it detects that the client has
failed to send a heart-beat in a timely manner.

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
![protocol](https://user-images.githubusercontent.com/69999501/229677265-9915c526-e9da-4353-8391-9b4a1fed241c.png)

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
* `serviceStatus` - get status of all services - accountService and wordService

The client will "ping" the server every 5 seconds to get a status update on the `wordService` and `accountService`.

The GameServer will then invoke the corresponding methods on either, `AccountService`, or `WordSerivce`

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
* `isAlive(): boolean`
  * Used to check if the service is alive
  * Returns - boolean to represent if service is alive
  
##### WordService
* `getPhrase(length:int): String`
  * Receives - number of words to generate for a phrase
  * Returns - a phrase that contains `length` words
* `addWord(word:String): Boolean`
  * Synchronized method
  * Receives - word to add to repository
  * Returns - boolean based on success or faliure of adding word
* `removeWord(word:String): Boolean`
  * Synchronized method
  * Receives - word to remove
  * Returns - boolean based on success or failure of removing word
* `checkWord(word:String): Boolean`
  * Synchronized method
  * Receives - word to check
  * Returns - boolean based on success or failure of existance of word
* `isAlive(): Boolean`
  * Used to check if the service is alive
  * Returns - boolean to represent if service is alive
  
##### GameHandlerService
* `startGame(player:String, number_of_words:int, failed_attempt_factor:int, seq:int): String`
  * Receives - player username, number of words they want in phrase, and how many attempts
  * Returns - phrase that has been converted into dashes (-) + attempt counter
* `guessLetter(player:String, letter:char, seq:int): String`
  * Receives - player username and guessed letter
  * Returns - letter in the correct place in the phrase + updated attempt counter
* `guessPhrase(player:String, phrase:String, seq:int): String`
  * Receives - player username and guessed phrase
  * Returns - correct/incorrect guess message + updated attempt counter
* `endGame(player:String, seq:int): String`
  * Removes player from `gameState`
  * Receives - player username
  * Returns - game ended message + next steps
* `restartGame(player:String,seq:int): String`
  * Removes player from `gameState`
  * Receives - player username
  * Returns - game restart message
* `addWord(word:String, seq:int): String`
  * Invokes `addWord` method in `WordService`
  * Receives - word to add to repository
  * Returns - String based on success or failure of adding word
* `removeWord(word:String, seq:int): String`
  * Invokes `removeWord` method in `WordService`
  * Receives - word to remove from repository
  * Returns - String based on success or failure of removing word
* `checkWord(word:String, seq:int): String`
  * Invokes `checkWord` method in `WordService`
  * Receives - word to check for in repository
  * Returns - String based on success or failure of finding word
* `login(username:String, password:String, seq:int): int`
  * Invokes `readFromFile` method in `AccountService`
  * Receives - username and password
  * Returns - integer representing whether user can be logged in or not
* `register(username:String, password:String, seq:int): String`
  * Invokes `writeToFile` method in`AccountService`
  * Receives - username and password
  * Returns - same message as `writeToFile`
* `logOut(String:username, seq:int): void`
  * Removes player username from loggedIn users
  * Receives - username
* `getScore(username:String, password:String, seq:int): String`
  * Receives - username and password
  * Returns - user's highscore
* `addClientListener(client:ClientListener, username:String): void`
  * Registers client listener for heartbeat mechanism
* `serviceStatus(): boolean`
  * Used to check if the services are alive (indifferent to which service)
  * Returns - boolean to represent if service is alive
#### Server - Client: 

##### AccountService : None
##### WordService: None
##### GameHandlerService:
* `ping(): void`
  * ping client for heartbeat checks
##### ClientListener:
* `ping(): void`
  * Receive ping from server for heartbeat checks

### Design choices
* `login()` - returning `int` instead of `boolean`:
An integer is being returned to allow us to determine if a user should be logged in or not. This allows us to ensure one session per user, as well catching any potential errors, and showing meaningful messages to the user
  1. No account - return 1
  2. Already logged in - return 2
  3. Game state is null - return 3
  4. Other error - return 4
* `ping()` - This method is a heartbeat that will periodically check if the client is still connected. It is important to have this because we are keeping track of usernames, and only allowing them to have one active session, meaning that if a `RemoteException` is thrown by any of the method calls and the client crashes, their username would not be removed from the `loggedInUsers` without this mechanism. This will be a problem as the user will not be able to login unless the `GameServer` is restarted. This method will prevent such event from occurring.
* `serviceStatus()` - This method is used to check if the services are alive, and is indifferent to which service has gone down. This is important because if either `wordService` or `accountService` have gone down, the client will not be able to connect to play the game. This method will allow the client to check if the services are alive, and if they are not, the client will be notified that the service is down, and to try again later.
* **Deduplication** - On the client side a random number between 1 - 50 is generated and assigned as the sequence number for that method invocations. All `GameHandlerService` method will take in the sequence number as a parameter. To simulate duplicate messages being sent to the server, the client will first invoke a method as it would before, but now it will do a check on the sequence number and based on that will re-invoke the same method 50% of the time. This will allow us to test the server for duplicate messages, and ensure that the server is able to handle them. In each `GameHandlerService` method, the method will get the sequence number that it initially stored and compare that to the incoming sequence number and will determine whether to execute the method or ignore - a message will be printed on the server side just to verify that this functionality is working. 
