/**
 * This class is used to create the AccountService and bind it to the RMI registry. It also creates
 * the file that will be used to store the user information.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class AccountServer {

  //Bound to port 4777
  public static void main(String[] args) {
    File file = new File("users.txt");
    try {
      if (file.createNewFile()) {
        System.out.println("Creating account file for users.");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      AccountService service = new AccountService();
      LocateRegistry.createRegistry(4777);
      Naming.rebind("rmi://localhost:4777" + "/AccountService", service);
      System.out.println("Account Service connected");
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
