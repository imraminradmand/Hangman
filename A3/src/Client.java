/**
 * The Client class is responsible for creating and managing the client-side connection with the
 * GameServer using the RMI (Remote Method Invocation) protocol.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Client extends UnicastRemoteObject implements ClientListener {

  private final GameHandlerInterface service;
  private String username;
  private String password;
  private final boolean canProceed;

  private Integer seq = new Random().nextInt(50);

  public Client(GameHandlerInterface service) throws RemoteException {
    this.service = service;
    this.canProceed = true;
  }


  /**
   * The main method is responsible for creating the client-side connection with the GameServer
   * using the RMI (Remote Method Invocation) protocol. The client is able to log in or register
   * with the server and then play the game.
   *
   * @param args the command line arguments
   * @throws IOException       if there is an error reading from the console
   * @throws NotBoundException if the naming service cannot find the remote object
   */
  public static void main(String[] args) throws IOException, NotBoundException {

    if (args.length < 1) {
      System.out.println("Usage: java Client <server ip>");
      System.exit(0);
    }

    GameHandlerInterface service = (GameHandlerInterface) Naming.lookup(
        "rmi://" + args[0] + ":4777" + "/GameServer");

    Client client = new Client(service);

    client.clientStart();

  }

  public void clientStart() throws IOException {

    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader stdin = new BufferedReader(inputStreamReader);

    // every 5 seconds call the server for a serviceStatus message
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (canProceed) {
          try {
            boolean result = service.serviceStatus();

            if (!result) {
              System.out.println("One of our services have gone down :( - come back again later");
              System.exit(1);
            }
          } catch (RemoteException e) {
            e.printStackTrace();
          }
        }
      }
    }, 0, 5000);

    while (true) {
      System.out.println(
          "USAGE: login <username> <password> OR register <username> <password> OR exit");
      String input = stdin.readLine();
      String[] clientArgs = input.split(" ");

      if (input.equalsIgnoreCase("exit")) {
        System.exit(0);
      } else if (clientArgs[0].equalsIgnoreCase("login")) {
        if (clientArgs.length == 3) {

          int code = service.login(clientArgs[1], clientArgs[2], seq);

          switch (code) {
            case 0:
              username = clientArgs[1];
              password = clientArgs[2];
              playGame(service);
              break;
            case 1:
              System.out.println("Account does not exist.");
              break;
            case 2:
              System.out.println("Account already logged in somewhere else.");
              break;
            case 3:
              System.out.println("Account gamestate is not null");
              break;
            default:
              System.out.println("Some internal error has occurred.");
          }
        }

      } else if (clientArgs[0].equalsIgnoreCase("register")) {
        if (clientArgs.length == 3) {
          if (service.register(clientArgs[1], clientArgs[2], seq)) {
            username = clientArgs[1];
            password = clientArgs[2];
            playGame(service);
            break;
          } else {
            System.out.println("Account already exists with that username.");
          }
        }
      } else {

        System.out.println("Invalid input.");
      }
    }
    inputStreamReader.close();
    stdin.close();
  }

  private void playGame(GameHandlerInterface service) throws IOException {

    registerClientToServer();

    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader stdin = new BufferedReader(inputStreamReader);
    System.out.println("Start a game with start <number of words> <attempts>");

    while (true) {
      String input = stdin.readLine();
      String[] args = input.split(" ");

      if (args[0].equalsIgnoreCase("exit")) {
        service.logOut(username, seq);
        seq++;
        int rand = new Random().nextInt(100);
        if (rand < 50) {
          service.logOut(username, seq - 1);
        }

        break;
      }

      if (args.length == 1) {
        /*
         * Check game commands that are only one character
         * */

        if (args[0].equals("!")) {
          System.out.println(service.restartGame(username, seq));
          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.restartGame(username, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }

        } else if (args[0].equals("#")) {
          System.out.println(service.endGame(username, seq));
          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.endGame(username, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }

        } else if (args[0].equals("!help")) {
          writeHelpScreen();
        } else if (args[0].equals("$")) {
          System.out.println(service.getScore(username, password, seq));
          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.getScore(username, password, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }

        } else if (input.length() == 1) {
          System.out.println(service.guessLetter(username, args[0].charAt(0), seq));
          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.guessLetter(username, args[0].charAt(0), seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }

        } else {
          System.out.println(service.guessPhrase(username, input, seq));
          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.guessPhrase(username, input, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }
        }
      } else if (args.length == 2) {
        /*
         * Write commands here that require two arguments such as ?word <word>
         * */
        if (args[0].equals("?")) {
          String word = args[1];
          System.out.println(word + ": " + service.checkWord(username, word, seq));

          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.checkWord(username, word, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }

        } else if (args[0].equals("+")) {
          String word = args[1];
          String response = service.addWord(username, word, seq);
          System.out.println(word + ": " + (response));

          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.addWord(username, word, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }

        } else if (args[0].equals("-")) {
          String word = args[1];
          String response = service.removeWord(username, word, seq);
          System.out.println(word + ": " + (response));

          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {

            String output = (service.removeWord(username, word, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }
        } else {
          System.out.println(service.guessPhrase(username, input, seq));

          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = service.guessPhrase(username, input, seq - 1);
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }
        }
      } else if (args.length == 3) {
        if (args[0].equalsIgnoreCase("start")) {
          System.out.println(
              service.startGame(username, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                  seq));
          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.startGame(username, Integer.parseInt(args[1]),
                Integer.parseInt(args[2]), seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }
        } else {
          System.out.println(service.guessPhrase(username, input, seq));
          seq++;
          int rand = new Random().nextInt(100);
          if (rand < 50) {
            String output = (service.guessPhrase(username, input, seq - 1));
            if (output.charAt(0) != '!') {
              System.out.println(output);
            }
          }
        }
      } else {
        System.out.println(service.guessPhrase(username, input, seq));
        seq++;
        int rand = new Random().nextInt(100);
        if (rand < 50) {
          String output = (service.guessPhrase(username, input, seq - 1));
          if (output.charAt(0) != '!') {
            System.out.println(output);
          }
        }
      }
    }
  }

  private void writeHelpScreen() {
    System.out.println("-----Commands-----");
    System.out.println("start <number of letters> <number of words>");
    System.out.println("# - Ends the game");
    System.out.println("? <word>- Checks if a word exists in the repository");
    System.out.println("+ <word>- Adds word to the repository");
    System.out.println("- <word>- Removes word from the repository");
    System.out.println("$ - Retrieves a users score");

  }


  private void registerClientToServer() throws RemoteException {
    service.addClientListener(this, username);

    Runnable pingServer = () -> {
      while (true) {
        try {
          service.ping();
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } catch (RemoteException e) {
          System.out.print("Connection error with server!");
          System.exit(0);
        }

      }
    };

    Thread pingServerT = new Thread(pingServer);
    pingServerT.start();
  }

  /**
   * This method is used for as a heartbeat from the server to ensure that the client is still
   * connected.
   *
   * @throws RemoteException - if there is an error with the connection
   */
  @Override
  public void ping() throws RemoteException {
    //System.out.println("pong");
  }

}
