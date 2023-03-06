import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WordServiceInterface extends Remote {

  String getPhrase(int length) throws RemoteException;

  boolean addWord(String word) throws RemoteException;

  boolean removeWord(String word) throws RemoteException;

  boolean checkWord(String word) throws RemoteException;
}
