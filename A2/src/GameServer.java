import java.io.IOException;
import java.rmi.Naming;

public class GameServer {

  private static AccountInterface accountService;

  public static void main(String[] args) throws IOException {

    GameHandlerService service = new GameHandlerService();
    Naming.rebind("rmi://localhost:4777" + "/GameServer", service);
    System.out.println("Game Server connected");
  }
}
