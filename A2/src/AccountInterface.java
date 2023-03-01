import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccountInterface extends Remote {
     boolean writeToFile(String[] args) throws IOException, RemoteException;
     String readFromFile(String[] args) throws RemoteException;
}
