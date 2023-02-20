import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class is the client side of the Hangman game. It will connect to the server and allow the
 * user to play the game. The user will be able to do the following: 1. Register a new account 2.
 * Login to an existing account 3. Play the game 4. Check the status of their account 6. Exit the
 * game
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */
public class Client {

  private static final String CLIENT_USAGE = "java Client [host] [port]";
  private Socket clientSocket;

  /**
   * Constructs a Client object and initializes the client socket to connect to the given host on
   * the given port.
   *
   * @param host the hostname or IP address of the server
   * @param port the port number on which the server is listening
   */
  public Client(String host, int port) {
    try {
      clientSocket = new Socket(host, port);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Handles all client side functionality. Login, registration, and server communication
   */
  private void run() {
    String username;
    String password;

    try {
      System.out.println("Connected");

      // Socket Side
      BufferedReader socketIn = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream()));
      PrintWriter socketOut = new PrintWriter(clientSocket.getOutputStream(), true);

      // User input
      InputStreamReader inputStreamReader = new InputStreamReader(System.in);
      BufferedReader stdin = new BufferedReader(inputStreamReader);

      String userInput;

      while (true) {
        try {
          // Display initial prompt and read/send response
          String prompt = socketIn.readLine();
          System.out.println(prompt);

          // prompt will come from server
          userInput = stdin.readLine();
          String[] clientArgs = userInput.split(" ");

          while (clientArgs.length < 3 && !userInput.equalsIgnoreCase("exit")) {
            System.out.println(prompt);
            userInput = stdin.readLine();
            clientArgs = userInput.split(" ");
          }

          // Break from loop if "EXIT" is input
          if (userInput.equalsIgnoreCase("EXIT")) {
            socketOut.println("exit");
            System.out.println("Closing connection...");
            break;
          } else if (clientArgs[0].equalsIgnoreCase("login")) {
            socketOut.println(userInput);
            username = clientArgs[1];
            password = clientArgs[2];

            // PLAY GAME
            if (!socketIn.readLine().equals("!noaccount!")) {
              System.out.println("Welcome back, " + username + "!");
              GameLogic.clientPlay(socketIn, stdin, socketOut, username, password);
              break;
            }

          } else if (clientArgs[0].equalsIgnoreCase("register")) {
            socketOut.println(userInput);
            username = clientArgs[1];
            password = clientArgs[2];

            if (!socketIn.readLine().equalsIgnoreCase("!fail!")) {
              GameLogic.clientPlay(socketIn, stdin, socketOut, username, password);
              break;
            }
          }
          // Invalid input
          else {
            socketOut.println(userInput);
            System.out.println(socketIn.readLine());
          }
          System.out.println();
        } catch (IOException e) {
          System.out.println(e);
        }
      }

      // Close connection
      try {
        stdin.close();
        socketIn.close();
        socketOut.close();
        clientSocket.close();
      } catch (IOException e) {
        System.out.println(e);
      }
    } catch (IOException e) {
      System.out.println("Error reading from input: " + e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Error processing input: Incorrect number of arguments");
    } catch (Exception e) {
      System.out.println("Unexpected error occurred: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    Client client;

    if (args.length != 2) {
      System.out.println(CLIENT_USAGE);
      System.exit(1);
    }

    try {
      client = new Client(
          args[0],
          Integer.parseInt(args[1])
      );
      client.run();
    } catch (NumberFormatException e) {
      System.err.println("Invalid port number: " + args[1] + ".");
      System.exit(1);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

}