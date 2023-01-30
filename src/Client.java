import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
  private static InetAddress host;
  private static final int PORT = 5555;

  public Client() throws UnknownHostException {
    String username = "";
    String password = "";

    host = InetAddress.getLocalHost();
    try {
      Socket socket = new Socket(host, PORT);
      System.out.println("Connected");

      // Socket Side
      BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);

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

          // Break from loop if "EXIT" is input
          if (userInput.equalsIgnoreCase("EXIT")) {
            System.out.println("Closing connection...");
            break;
          } else if (clientArgs[0].equalsIgnoreCase("login")) {
            socketOut.println(userInput);
            username = clientArgs[1];
            password = clientArgs[2];
            // start playing the game - will be refactored
            if (socketIn.readLine() != null) {
              System.out.println(socketIn.readLine());
              while (true) {
                String res = stdin.readLine();
                String[] resArgs = res.split(" ");

                if (resArgs[0].equals("start")) {
                  socketOut.println(res);
                  System.out.println("Phrase from repo: " + socketIn.readLine());
                } else if (resArgs[0].equals("?")) {
                  socketOut.println(res);
                  System.out.println(resArgs[1] + ": " + socketIn.readLine());
                } else if (res.equalsIgnoreCase("$")) {
                  socketOut.println("$ " + username + " " + password);
                  System.out.println(socketIn.readLine());
                } else if (res.equalsIgnoreCase("#")) {
                  socketOut.println(res);
                  System.out.println("Exiting...");
                  break;
                }
              }
            }
          } else if (clientArgs[0].equalsIgnoreCase("register")) {
            socketOut.println(userInput);
            System.out.println(socketIn.readLine());
          }
          // If the user does not input "PLAY" or "EXIT"
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
        socket.close();
      } catch (IOException e) {
        System.out.println(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public static void main (String[] args) throws UnknownHostException {
    Client client = new Client();
  }
}
