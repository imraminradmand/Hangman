import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class WordServer {

  public static void main (String[] args) {
    try {
      WordService wordService = new WordService();
      LocateRegistry.createRegistry(4777);
      Naming.rebind("rmi://localhost:4777" + "/WordService", wordService);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
