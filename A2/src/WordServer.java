/**
 * The WordServer class serves as the entry point for the application. It is responsible for
 * starting up the WordService and binding it to a naming service.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.rmi.Naming;

public class WordServer {

  public static void main(String[] args) {
    try {
      WordService wordService = new WordService();
      Naming.rebind("rmi://localhost:4777" + "/WordService", wordService);
      System.out.println("Word Service connected");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
