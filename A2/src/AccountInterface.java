import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccountInterface extends Remote {
     boolean writeToFile(String username, String password, String score) throws IOException, RemoteException;
     String readFromFile(String username, String password) throws RemoteException;
     void updateScore(String username, int score) throws IOException;
}
