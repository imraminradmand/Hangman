import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * ClientHandler is a class that implements the Runnable interface and is used to handle client connections to the server.
 * It listens to the client and provides the required response.
 * It connects to the Account Service and Word Service to provide the required functionalities.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */
public class ClientHandler implements Runnable {

  private final Socket clientSocket;
  private final int WORD_SERVICE_PORT;
  private final int ACCOUNT_SERVICE_PORT;

  /**
   * Constructor for ClientHandler class.
   *
   * @param socket           Socket to connect to the client.
   * @param wordServicePort  Port to connect to the Word Service.
   * @param accountServicePort Port to connect to the Account Service.
   */
  ClientHandler(Socket socket, int wordServicePort, int accountServicePort) {
    this.clientSocket = socket;
    this.WORD_SERVICE_PORT = wordServicePort;
    this.ACCOUNT_SERVICE_PORT = accountServicePort;
  }

  @Override
  public void run() {
    System.out.println("Connected, listening to client at: " + clientSocket);
    try {
      InputStreamReader socketInputReader = new InputStreamReader(clientSocket.getInputStream());
      OutputStream socketOutStream = clientSocket.getOutputStream();

      BufferedReader socketInput = new BufferedReader(socketInputReader);
      PrintWriter socketOutput = new PrintWriter(socketOutStream, true);

      // String to read message from client
      String clientResponse;

      Socket accountSocket = new Socket(InetAddress.getLocalHost(), ACCOUNT_SERVICE_PORT);
      BufferedReader accountIn = new BufferedReader(
          new InputStreamReader(accountSocket.getInputStream()));
      PrintWriter accountOut = new PrintWriter(accountSocket.getOutputStream(), true);

      // While loop to keep reading until "EXIT" is input
      while (true) {
        try {
          socketOutput.println("Instruction: <login/register> <username> <password> OR EXIT");
          // Initial prompt and read response
          clientResponse = socketInput.readLine();

          if (clientResponse != null) {
            String[] args = clientResponse.split(" ");

            if (args[0].equalsIgnoreCase("exit")) {
              accountOut.println("exit");

              break;
            } else if (args[0].equalsIgnoreCase("login")) {
              accountOut.println("get " + args[1] + " " + args[2]);

              String accountResponse = accountIn.readLine();
              socketOutput.println(accountResponse);

              // if valid account start game
              if (!accountResponse.equals("!noaccount!")) {
                GameLogic.serverPlay(socketInput, socketOutput,
                    accountIn, accountOut, args, WORD_SERVICE_PORT, clientSocket);
                return;
              }

              // register user, if successful start game
            } else if (args[0].equalsIgnoreCase("register")) {
              accountOut.println("get " + args[1] + " " + args[2]);

              if (accountIn.readLine().equals("!noaccount!")) {

                accountOut.println("post " + args[1] + " " + args[2] + " 0");
                String accountResponse = accountIn.readLine();

                if (accountResponse.equals("!success!")) {
                  socketOutput.println(accountResponse);
                  GameLogic.serverPlay(socketInput, socketOutput,
                      accountIn, accountOut, args, WORD_SERVICE_PORT, clientSocket);
                  return;
                } else {
                  socketOutput.println("!fail!");
                }
              } else {
                socketOutput.println("!fail!");
              }
            } else {
              socketOutput.println("Unknown Command, try again");
            }
          }
        } catch (IOException e) {
          System.out.println("Error reading from input: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println("Error processing input: Incorrect number of arguments");
        } catch (Exception e) {
          System.out.println("Unexpected error occurred: " + e.getMessage());
        }
      }
      // Close connection
      socketInput.close();
      socketOutput.close();
      clientSocket.close();

      accountIn.close();
      accountOut.close();
      accountSocket.close();
    } catch (Exception e) {
      System.out.println("Unexpected error occurred: " + e.getMessage());
      System.out.println("Error type: " + e.getClass().getName());
      System.out.println("Stack trace:");
      for (StackTraceElement element : e.getStackTrace()) {
        System.out.println("\t" + element.toString());
      }
    }
  }
}
