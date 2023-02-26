import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccountInterface extends Remote {
    public boolean writeToFile(String[] args) throws IOException, RemoteException;
    public String readFromFile(String[] args) throws RemoteException;
}
