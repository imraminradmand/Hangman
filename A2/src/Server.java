import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Server class is responsible for creating a server socket and handling incoming connections
 * from clients. The server accepts incoming connections and assigns each connection to a new thread
 * for processing. The server also takes in three command line arguments representing the port to
 * listen on, the port for the word service, and the port for the account service.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */
public class Server {

  private static final String USAGE = "java Server [port] [WordService port] [AccountService port]";

  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println(USAGE);
      System.exit(1);
    }

    int port = 0;
    int wordServicePort;
    int accountServicePort;
    ServerSocket serverSocket;

    try {
      port = Integer.parseInt(args[0]);
      wordServicePort = Integer.parseInt(args[1]);
      accountServicePort = Integer.parseInt(args[2]);
      serverSocket = new ServerSocket(port);
      System.out.println("Server is running...");
      ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

      while (true) {
        fixedThreadPool.execute(
            new ClientHandler(serverSocket.accept(), wordServicePort, accountServicePort));
      }
    } catch (IOException e) {
      System.out.println(
          "Exception caught when trying to listen on port " + port
              + " or listening for a connection");
      System.out.println(e.getMessage());
    }
  }
}
