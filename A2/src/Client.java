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

public class Client extends UnicastRemoteObject implements ClientListener {

  private final GameHandlerInterface service;
  private String username;
  private String password;

  public Client(GameHandlerInterface service) throws RemoteException {
    this.service = service;
  }


  /**
   * This method is used for as a heartbeat to the server to ensure that the client is still
   * connected.
   *
   * @throws RemoteException - if there is an error with the connection
   */
  @Override
  public void ping() throws RemoteException {
    //System.out.println("pong");
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

    GameHandlerInterface service = (GameHandlerInterface) Naming.lookup(
        "rmi://localhost:4777" + "/GameServer");

    Client client = new Client(service);

    client.clientStart();

  }

  public void clientStart() throws IOException, NotBoundException {

    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader stdin = new BufferedReader(inputStreamReader);

    while (true) {

      System.out.println(
          "USAGE: login <username> <password> OR register <username> <password> OR exit");
      String input = stdin.readLine();
      String[] clientArgs = input.split(" ");

      if (input.equalsIgnoreCase("exit")) {
        System.exit(0);
      } else if (clientArgs[0].equalsIgnoreCase("login")) {
        if (clientArgs.length == 3) {

          int code = service.login(clientArgs[1], clientArgs[2]);

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
          if (service.register(clientArgs[1], clientArgs[2])) {
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
        service.logOut(username);
        break;
      }

      if (args.length == 1) {
        /*
         * Check game commands that are only one character
         * */

        if (args[0].equals("!")) {
          System.out.println(service.restartGame(username));
        } else if (args[0].equals("#")) {
          System.out.println(service.endGame(username));
        } else if (args[0].equals("!help")) {
          writeHelpScreen();
        } else if (args[0].equals("$")) {
          System.out.println(service.getScore(username, password));
        } else if (input.length() == 1) {
          System.out.println(service.guessLetter(username, args[0].charAt(0)));
        } else {
          System.out.println(service.guessPhrase(username, input));
        }
      } else if (args.length == 2) {
        /*
         * Write commands here that require two arguments such as ?word <word>
         * */
        if (args[0].equals("?")) {
          String word = args[1];
          System.out.println(word + ": " + service.checkWord(word));
        } else if (args[0].equals("+")) {
          String word = args[1];
          boolean response = service.addWord(word);
          System.out.println(word + ": " + (response ? "added" : "exists"));
        } else if (args[0].equals("-")) {
          String word = args[1];
          boolean response = service.removeWord(word);
          System.out.println(word + ": " + (response ? "removed" : "does not exist"));
        } else {
          System.out.println(service.guessPhrase(username, input));
        }
      } else if (args.length == 3) {
        if (args[0].equalsIgnoreCase("start")) {
          System.out.println(
              service.startGame(username, Integer.parseInt(args[1]), Integer.parseInt(args[2])));
        } else {
          System.out.println(service.guessPhrase(username, input));
        }
      } else {
        System.out.println(service.guessPhrase(username, input));
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

}
