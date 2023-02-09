import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

  private static final String WORD_CHECK_USAGE = "? <word to check for>";
  private static final String CLIENT_USAGE = "java Client [host] [port]";
  private Socket clientSocket;
  public Client(String host, int port) {
    try {
      clientSocket = new Socket(host, port);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  private void run() {
    String username = "";
    String password = "";

    try {
      System.out.println("Connected");

      // Socket Side
      BufferedReader socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

          while (clientArgs.length < 3 && !userInput.equalsIgnoreCase("exit")){
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
              play(socketIn, stdin, socketOut, username, password);
              break;
            }

          } else if (clientArgs[0].equalsIgnoreCase("register")) {
            socketOut.println(userInput);
            username = clientArgs[1];
            password = clientArgs[2];

            if (!socketIn.readLine().equalsIgnoreCase("!fail!")) {
              play(socketIn, stdin, socketOut, username, password);
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

      if(resArgs[0].equalsIgnoreCase("exit")){
        socketOut.println("exit");

        break;
      } else if (resArgs[0].equals("start")) {
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
          } else if (play.equalsIgnoreCase("!")) {
            socketOut.println(play);
            System.out.println(socketIn.readLine());
            break;
          }

          socketOut.println(play);
          String display = socketIn.readLine();

          if (display.charAt(0) == ('#')) {
            System.out.println("You lose, phrase was: " + display.replace("#", ""));
            System.out.println("Exiting...");
            return;
          } else if (display.charAt(0) == ('!')) {
            System.out.println("You win!");
            gameOver = true;
          } else {
            System.out.println(display.replace("#", ""));
          }
          System.out.println("Enter guess or a command: ");
        }

      } else if (resArgs[0].equals("?")) {
        socketOut.println(res);
        System.out.println(resArgs[1] + ": " + socketIn.readLine());
      } else if (res.equalsIgnoreCase("$")) {
        socketOut.println("$ " + username + " " + password);
        System.out.println(socketIn.readLine());
      } else if (res.equalsIgnoreCase("#")) {
        socketOut.println(res);
        break;
      } else if (resArgs[0].equalsIgnoreCase("+") || resArgs[0].equalsIgnoreCase("-")) {
        socketOut.println(res);
        System.out.println(socketIn.readLine());
      }
    }
  }

  public static void main(String[] args) throws IOException {
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