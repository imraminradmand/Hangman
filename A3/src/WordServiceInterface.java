/**
 * The WordServiceInterface defines the remote interface for a word service. It provides methods to
 * get a random phrase of a certain length, add a new word to the service, remove a word from the
 * service, and check if a word is in the service.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WordServiceInterface extends Remote {

  String getPhrase(int length) throws RemoteException;

  boolean addWord(String word) throws RemoteException;

  boolean removeWord(String word) throws RemoteException;

  boolean checkWord(String word) throws RemoteException;

  boolean isAlive() throws RemoteException;
}
