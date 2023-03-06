import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WordInterface extends Remote {
  public void populateWords() throws RemoteException;
  public String getPhrase(int length) throws RemoteException;
  public boolean addWord(String word) throws RemoteException;
  public boolean removeWord(String word) throws RemoteException;
  public boolean checkWord(String word) throws RemoteException;
}
