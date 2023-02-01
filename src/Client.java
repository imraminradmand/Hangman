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
              play(socketIn, stdin, socketOut, username, password);
            }

            // TODO: Check for successful registration then start game
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
        System.out.println(e);
        System.exit(0);

    }
  }

  // TODO: add actual guessing logic
  private void play(BufferedReader socketIn,
      BufferedReader stdin,
      PrintWriter socketOut,
      String username, String password) throws IOException {
	  
      
    while (true) {

        System.out.println(socketIn.readLine());
        String res = stdin.readLine();
        String[] resArgs = res.split(" ");
        
        socketOut.println(res);
        
      // TODO: Actual logic goes under the start condition
      if (resArgs[0].equals("start") && resArgs.length == 3) {
          System.out.println(socketIn.readLine());
          boolean gameOver = false;
          
          //gameplay
          while(!gameOver) {
              String play = stdin.readLine();
              
              //check input for special commands
              if (res.equalsIgnoreCase("$")) {
                  play = ("$ " + username + " " + password);
                } else if (res.equalsIgnoreCase("#")) {
                  socketOut.println(res);
                  System.out.println("Exiting...");
                }
              
              socketOut.println(play);
              String display = socketIn.readLine();

              
              if(display.charAt(0) == ('#')) {
            	  System.out.println("You lose, phrase was: " + display.replace("#", ""));
            	  gameOver = true;
            	  
              }else if(display.charAt(0) == ('!')) {
            	  System.out.println("You win!");
            	  gameOver = true;
              }else {
            	  System.out.println(display.replace("#", ""));
              }
          }
          
      }else {
    	  System.out.println("wrong command, try again");
      }
    }
  }
  public static void main (String[] args) throws UnknownHostException {
    Client client = new Client();
  }
}
