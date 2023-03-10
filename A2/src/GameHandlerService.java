/**
 * This class implements the GameHandlerInterface and provides the logic for the game server. It
 * keeps track of the game states of players, allows them to start, end, and restart games, and
 * manages the account and word services for player authentication and word management.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GameHandlerService extends UnicastRemoteObject implements GameHandlerInterface {

  private final ArrayList<GameObject> gameStates = new ArrayList<>();
  private final Set<String> loggedInUsers = new HashSet<>();
  private final AccountInterface accountService;
  private final WordServiceInterface wordService;

  protected GameHandlerService() throws RemoteException {
    super();
    try {
      accountService = (AccountInterface) Naming.lookup("rmi://localhost:4777" + "/AccountService");
      wordService = (WordServiceInterface) Naming.lookup("rmi://localhost:4777" + "/WordService");
    } catch (NotBoundException | MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method starts a game for a player. It generates a random word of the specified length and
   * adds it to the gameStates list.
   *
   * @param player                - the player starting the game
   * @param number_of_words       - the number of the word to be generated
   * @param failed_attempt_factor - the number of failed attempts allowed
   * @return the stringified word to be displayed to the player
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public String startGame(String player, int number_of_words, int failed_attempt_factor)
      throws RemoteException {
    String randomWord = wordService.getPhrase(number_of_words);
    System.out.println(randomWord);
    gameStates.add(new GameObject(player, number_of_words, failed_attempt_factor, randomWord));
    return Objects.requireNonNull(getPlayerState(player)).getStringifyedWord();
  }

  /**
   * This method checks if the letter the player has guessed is in the word. If it is, it returns
   * the stringified word with the letter in the correct position. Otherwise, it will either return
   * the stringified word with the counter decremented or says the letter has been already guessed.
   *
   * @param player - the player guessing the letter
   * @param letter - the letter the player has guessed
   * @return the stringified word to be displayed to the player or a message saying the letter has
   * already been guessed or the player has run out of attempts
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public String guessLetter(String player, char letter) throws RemoteException {
    GameObject gameState = getPlayerState(player);

    if (gameState != null) {
      int attempts = gameState.getAttempts();
      if (attempts < 2) {
        return endGame(player);
      }
      if (gameState.guessLetter(Character.toLowerCase(letter))) {
        if (gameState.getStringifyedWord()
            .contains("Usage: start <number of letters> <attempts>")) {
          try {
            accountService.updateScore(gameState.getUsername());
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          removeGameState(gameState.getUsername());
        }
        return gameState.getStringifyedWord();
      } else if (gameState.alreadyGuessed(Character.toLowerCase(letter))) {
        return "Letter already guessed\n" + gameState.getStringifyedWord();
      }
      return "Incorrect guess!\n" + gameState.getStringifyedWord();
    }
    return "Gamestate is null, try starting a new game!";
  }

  /**
   * This method checks if the whole phrase the player has guessed is correct or not
   *
   * @param player - the player guessing the phrase
   * @param phrase - the phrase the player has guessed
   * @return message whether user guessed the correct phrase or not
   * @throws IOException - if the updateScore method fails to write to users.txt file
   */
  @Override
  public String guessPhrase(String player, String phrase) throws IOException {
    GameObject gameState = getPlayerState(player);
    if (gameState != null) {
      int attempts = gameState.getAttempts();
      if (attempts == 0) {
        return endGame(player);
      }
      if (phrase.equalsIgnoreCase(gameState.getWord())) {
        accountService.updateScore(gameState.getUsername());
        removeGameState(gameState.getUsername());
        return "You guessed the correct phrase.\nUsage: start <number of letters> <attempts> or exit";
      } else {
        return "You guessed the incorrect phrase.";
      }
    }
    return "Gamestate is null, try starting a new game!";
  }

  /**
   * This method ends the game for a player and removes the game state from the gameStates list.
   *
   * @param player - the player ending the game
   * @return the word that the player was trying to guess
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public String endGame(String player) throws RemoteException {
    GameObject gameState = getPlayerState(player);
    removeGameState(player);

    assert gameState != null;
    return "Game ended, the word was: " + gameState.getWord()
        + "\nUsage: start <number of letters> <attempts> or exit";
  }

  /**
   * This method allows user to restart a game
   *
   * @param player - the player restarting the game
   * @return message restarting the game
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public String restartGame(String player) throws RemoteException {
    String response = "";
    GameObject gameState = getPlayerState(player);
    if (gameState == null) {
      response += "Start a game before attempting to restart\nUsage: start <number of letters> <attempts> or exit";
    } else {
      response += "Restarting game, the correct word was: " + gameState.getWord()
          + "\nUsage: start <number of letters> <attempts> or exit";
      removeGameState(player);
    }
    return response;
  }

  /**
   * This method adds a word to the word repository
   *
   * @param word - the word to be added
   * @return true if the word was added successfully, false otherwise
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public boolean addWord(String word) throws RemoteException {
    return wordService.addWord(word);
  }

  /**
   * This method removes a word from the word repository
   *
   * @param word - the word to be removed
   * @return true if the word was removed successfully, false otherwise
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public boolean removeWord(String word) throws RemoteException {
    return wordService.removeWord(word);
  }

  /**
   * This method checks if a word is in the word repository
   *
   * @param word - the word to be checked
   * @return true if the word is in the repository, false otherwise
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public boolean checkWord(String word) throws RemoteException {
    return wordService.checkWord(word);
  }

  /**
   * This method checks if a user is logged in. This will prevent users from logging in multiple
   * times.
   *
   * @param username - the username of the user
   * @param password - the password of the user
   * @return codes to determine if account can/can not login: 0: Can log in. 1: Account does not
   * exist. 2: Account already logged in 3: Account player state is null.
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public int login(String username, String password) throws RemoteException {

    if (accountService.readFromFile(username, password).equals("!noaccount!")) {
      return 1;
    } else if (loggedInUsers.contains(username)) {
      return 2;
    } else if (getPlayerState(username) != null) {
      return 3;
    } else {
      loggedInUsers.add(username);
      return 0;
    }

  }

  /**
   * This method registers a new user. It checks if the user already exists and if not, it creates a
   * new account for the user.
   *
   * @param username - the username of the user
   * @param password - the password of the user
   * @return true if the user was registered successfully, false otherwise
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public boolean register(String username, String password) throws RemoteException {
    if (!accountService.readFromFile(username, password).equals("!noaccount!")) {
      return false;
    }
    try {
      accountService.writeToFile(username, password, "0");
      loggedInUsers.add(username);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return true;
  }

  /**
   * This method logs a user out of the system, and remove them from the loggedInUsers set.
   *
   * @param username - the username of the user
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public void logOut(String username) throws RemoteException {
    loggedInUsers.remove(username);
  }

  /**
   * This method returns the high score of a user
   *
   * @param username - the username of the user
   * @param password - the password of the user
   * @return the high score of the user
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public String getScore(String username, String password) throws RemoteException {
    return "Your high score is " + accountService.readFromFile(username, password);
  }


  private GameObject getPlayerState(String username) {
    for (GameObject obj : gameStates) {
      if (obj.getUsername().equals(username)) {
        return obj;
      }
    }
    return null;
  }


  private void removeGameState(String username) {
    gameStates.removeIf(obj -> obj.getUsername().equals(username));
  }

  @Override
  public void addClientListener(ClientListener client, String username) throws RemoteException {
    Runnable pingClient = () -> {
      while (true) {
        try {
          client.ping();

          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

        } catch (RemoteException e) {
          try {
            this.removeGameState(username);
            this.logOut(username);
          } catch (RemoteException e1) {
            e1.printStackTrace();
          }
          System.out.println("Connection error or client has disconnected");
          return;
        }

      }
    };

    Thread pingClientT = new Thread(pingClient);
    pingClientT.start();

  }

  /**
   * This method is used to ping the client to check if the connection is still alive.
   *
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public void ping() throws RemoteException {
    //System.out.println("pong");
  }
}
