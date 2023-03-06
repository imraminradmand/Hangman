import java.rmi.Naming;

public class WordServer {

  public static void main (String[] args) {
    try {
      WordService wordService = new WordService();
      Naming.rebind("rmi://localhost:4777" + "/WordService", wordService);
      System.out.println("Word Service connected");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
