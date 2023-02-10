import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class handles the creation of the user file if it doesn't exist but also creates the thread
 * pool that will be used to handle each server instance. When a client is accepted the socket is
 * passed into a separate thread so that file IO can be properly handled.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

public class AccountsService {

  private static final String USAGE = "java AccountService [port]";

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println(USAGE);
      System.exit(1);
    }

    int port = 0;

    ServerSocket serverSocket;

    File file = new File("users.txt");
    file.createNewFile();

    try {
      port = Integer.parseInt(args[0]);
      serverSocket = new ServerSocket(port);

      System.out.println("Server is running...");
      ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

      while (true) {
        fixedThreadPool.execute(new AccountHandler(serverSocket.accept()));
      }
    } catch (IOException e) {
      System.out.println(
          "Exception caught when trying to listen on port " + port
              + " or listening for a connection");
      System.out.println(e.getMessage());
    }
  }

}
