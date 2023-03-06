import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameHandlerInterface extends Remote {

  String startGame(
      String player,
      int number_of_words,
      int failed_attempt_factor
  ) throws RemoteException;

  String guessLetter(String player, char letter) throws RemoteException;

  String guessPhrase(String player, String phrase) throws IOException;

  String endGame(String player) throws RemoteException;

  String restartGame(String player) throws RemoteException;

  boolean addWord(String word) throws RemoteException;

  boolean removeWord(String word) throws RemoteException;

  boolean checkWord(String word) throws RemoteException;

  boolean login(String username, String password) throws RemoteException;

  boolean register(String username, String password) throws RemoteException;

  void logOut(String username) throws RemoteException;

  String getScore(String username, String password) throws RemoteException;

}
