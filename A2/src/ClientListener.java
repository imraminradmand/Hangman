import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientListener extends Remote {
  void ping() throws RemoteException;
}