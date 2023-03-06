import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WordServiceInterface extends Remote {
  public String getPhrase(int length) throws RemoteException;
  public boolean addWord(String word) throws RemoteException;
  public boolean removeWord(String word) throws RemoteException;
  public boolean checkWord(String word) throws RemoteException;
}
