/**
 * This interface is used to define the methods that will be used by the AccountService.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccountInterface extends Remote {

  boolean writeToFile(String username, String password, String score)
      throws IOException;

  String readFromFile(String username, String password) throws RemoteException;

  void updateScore(String username) throws IOException;

  boolean isAlive() throws RemoteException;
}
