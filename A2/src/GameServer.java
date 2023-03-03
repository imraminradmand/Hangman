import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class GameServer {
    private static AccountInterface accountService;
    public static void main (String[] args) throws IOException {

        GameHandlerService service = new GameHandlerService();
        LocateRegistry.createRegistry(4777);
        Naming.rebind("rmi://localhost:4777" + "/GameServer", service);

    }
}
