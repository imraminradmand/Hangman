import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

  private static InetAddress host;
  private static final int PORT = 5555;
  private static final String WORD_CHECK_USAGE = "? <word to check for>";

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

            // PLAY GAME
            if (socketIn.readLine() != null) {
              System.out.println("Welcome back, " + username + "!");
              play(socketIn, stdin, socketOut, username, password);
            }

          } else if (clientArgs[0].equalsIgnoreCase("register")) {
            socketOut.println(userInput);
            username = clientArgs[1];
            password = clientArgs[2];

            if (socketIn.readLine().equalsIgnoreCase("!success!")) {
              play(socketIn, stdin, socketOut, username, password);
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
        socket.close();
      } catch (IOException e) {
        System.out.println(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void play(BufferedReader socketIn,
      BufferedReader stdin,
      PrintWriter socketOut,
      String username, String password) throws IOException {

    while (true) {
      System.out.println(socketIn.readLine());

      String res = stdin.readLine();
      String[] resArgs = res.split(" ");

      if (resArgs[0].equals("start")) {
        socketOut.println(res);
        System.out.println(socketIn.readLine());
        boolean gameOver = false;

        //gameplay
        while (!gameOver) {
          String play = stdin.readLine();
          String[] playArgs = play.split(" ");

          //check input for special commands
          if (play.equalsIgnoreCase("$")) {
            play = ("$ " + username + " " + password);
          } else if (play.equalsIgnoreCase("#")) {
            socketOut.println(play);
          } else if (play.equalsIgnoreCase("?")) {
            if (playArgs.length == 2) {
              play = ("? " + playArgs[1]);
            } else {
              System.out.println(WORD_CHECK_USAGE);
              play = stdin.readLine();
            }
          }

          socketOut.println(play);
          String display = socketIn.readLine();

          if (display.charAt(0) == ('#')) {
            System.out.println("You lose, phrase was: " + display.replace("#", ""));
            gameOver = true;

          } else if (display.charAt(0) == ('!')) {
            System.out.println("You win!");
            gameOver = true;
          } else {
            System.out.println(display.replace("#", ""));
          }
        }

      } else if (resArgs[0].equals("?")) {
        socketOut.println(res);
        System.out.println(resArgs[1] + ": " + socketIn.readLine());
      } else if (res.equalsIgnoreCase("$")) {
        socketOut.println("$ " + username + " " + password);
        System.out.println(socketIn.readLine());

        // TODO: FIX THIS, NOT QUITING AS EXPECTED, says exiting but then says incorrect try again. and jumps out to the login again
      } else if (res.equalsIgnoreCase("#")) {
        socketOut.println(res);
        break;
      }
    }
  }

  public static void main(String[] args) throws UnknownHostException {
    Client client = new Client();
  }
}