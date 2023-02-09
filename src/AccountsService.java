import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountsService {

    /*
    Main function:

    This method handles the creation of the user file if it doesn't exist but also creates the thread pool that will be used to handle each server instance.

    When a client is accepted the socket is passed into a separate thread so that file IO can be properly handled.

     */

  private static final int PORT = 7777;

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket;

    File file = new File("users.txt");
    file.createNewFile();

    try {

      serverSocket = new ServerSocket(PORT);

      System.out.println("Server is running...");
      ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

      while (true) {
        fixedThreadPool.execute(new AccountHandler(serverSocket.accept()));
      }
    } catch (IOException e) {
      System.out.println(
          "Exception caught when trying to listen on port " + PORT
              + " or listening for a connection");
      System.out.println(e.getMessage());
    }
  }

}
