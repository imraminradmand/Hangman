/**
 * The GameServer class is responsible for starting the game server and binding the
 * GameHandlerService object to the RMI registry. The server listens for incoming client connections
 * and routes them to the appropriate service.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.io.IOException;
import java.rmi.Naming;

public class GameServer {

  public static void main(String[] args) throws IOException {

    GameHandlerService service = new GameHandlerService();
    Naming.rebind("rmi://localhost:4777" + "/GameServer", service);
    System.out.println("Game Server connected");
  }
}
