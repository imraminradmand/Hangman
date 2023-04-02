/**
 * This interface represents the methods that can be remotely invoked on the Game server. The
 * methods handle all game related functionality, including starting a game, guessing a letter,
 * guessing a phrase, ending a game, restarting a game, adding a word, removing a word, checking a
 * word, logging in, registering, logging out, and getting a score.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameHandlerInterface extends Remote {

  String startGame(
      String player,
      int number_of_words,
      int failed_attempt_factor,
      int seq
  ) throws RemoteException;

  String guessLetter(String player, char letter, int seq) throws RemoteException;

  String guessPhrase(String player, String phrase, int seq) throws IOException;

  String endGame(String player, int seq) throws RemoteException;

  String restartGame(String player, int seq) throws RemoteException;

  String addWord(String player, String word, int seq) throws RemoteException;

  String removeWord(String player, String word, int seq) throws RemoteException;

  String checkWord(String player, String word, int seq) throws RemoteException;

  int login(String username, String password, int seq) throws RemoteException;

  boolean register(String username, String password, int seq) throws RemoteException;

  void logOut(String username, int seq) throws RemoteException;

  String getScore(String username, String password, int seq) throws RemoteException;

  void addClientListener(ClientListener client, String username) throws RemoteException;

  void ping() throws RemoteException;

}
