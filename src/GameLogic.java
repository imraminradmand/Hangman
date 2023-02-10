import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class contains all the game logic. The play method is called by the ClientHandler. It is
 * responsible for reading the client's requests and sending the appropriate responses,
 * communicating with both AccountService and WordService.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

public class GameLogic {

  private static final String WORD_CHECK_USAGE = "? <word to check for>";

  /**
   * Based on the response from the WordService, determine if word requested by the user to be
   * checked exists or not.
   *
   * @param inputBuf        inputBuffer
   * @param input           user request
   * @param wordRepository  DatagramSocket for WordService
   * @param wordServicePort port that WordService is running on
   * @return String exists
   * @throws IOException if error occurs on WordService side
   */
  private static String checkWord(byte[] inputBuf,
      String input,
      DatagramSocket wordRepository, int wordServicePort) throws IOException {

    String response = responseFromWordRepository(inputBuf, input, wordRepository, wordServicePort);
    String exists = "False";
    if (response.equalsIgnoreCase("true")) {
      exists = "True";
    }

    return exists;
  }

  /**
   * Checks the users high-score through AccountService.
   *
   * @param accountOut writer to AccountService
   * @param accountIn  reader from AccountService
   * @param args       arguments initially passed in for login
   * @return String high-score
   * @throws IOException if error occurs on AccountService side
   */
  private static String checkScore(PrintWriter accountOut, BufferedReader accountIn, String[] args)
      throws IOException {

    accountOut.println("get " + args[1] + " " + args[2]);

    String[] result = accountIn.readLine().split(" ");
    if (result.length > 1) {
      return ("High-score for " + result[0] + " is " + result[2]);
    }
    return "No high-score";
  }

  /**
   * Responsible for sending and reading back all WordService requests.
   *
   * @param inputBuf        inputBuffer
   * @param input           user input
   * @param wordRepository  DatagramSocket to communicate with WordService
   * @param wordServicePort port that WordService is running on
   * @return String response from WordService
   * @throws IOException if error occurs on WordService side.
   */
  private static String responseFromWordRepository(byte[] inputBuf,
      String input,
      DatagramSocket wordRepository, int wordServicePort) throws IOException {
    byte[] buf = input.getBytes();

    InetAddress address = InetAddress.getByName("localhost");
    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, wordServicePort);
    wordRepository.send(packet);

    DatagramPacket wordRepoReply = new DatagramPacket(inputBuf, inputBuf.length, address,
        wordServicePort);
    wordRepository.receive(wordRepoReply);

    return new String(wordRepoReply.getData(), 0, wordRepoReply.getLength());
  }

  /**
   * update the user's high-score
   *
   * @param accountIn  reader from AccountService
   * @param accountOut writer to AccountService
   * @param args       initial arguments passed in by user
   * @throws IOException if error occurs on AccountService side
   */
  private static void updateScore(BufferedReader accountIn, PrintWriter accountOut, String[] args)
      throws IOException {
    accountOut.println("get " + args[1] + " " + args[2]);

    String accountResponse = accountIn.readLine();
    String[] ar = accountResponse.split(" ");
    int val = Integer.parseInt(ar[2]);
    val += 100;
    accountOut.println("post " + args[1] + " " + args[2] + " " + val);
    accountIn.readLine();
  }

  /**
   * Houses all the game logic, and how to handle each request from the client.
   * <p>
   * The client can send the following commands:
   * <ul>
   *   <li>start <number of words> <attempts> - starts a new game</li>
   *   <li>? <word> - checks if the word exists in the word repository</li>
   *   <li>$ - returns the high score for the given username</li>
   *   <li>! - abandon current game and start new game</li>
   *   <li># - abandon current game and exit</li>
   *   <li>+ <word> - add word to word repository</li>
   *   <li>- <word> - remove word from word repository</li>
   *   <li>exit - exit the game</li>
   * </ul>
   * </p>
   * The client can also send a letter or a word to guess the phrase.
   * <p>
   *   The server can send the following responses:
   *   <ul>
   *     <li>True - if the word exists in the word repository</li>
   *     <li>False - if the word does not exist in the word repository</li>
   *     <li>High-score for <username> is <score> - if the user has a high score</li>
   *     <li>No highscore - if the user does not have a high score</li>
   *     <li>error - if the command is not valid</li>
   *     <li>Word added - if the word was successfully added to the word repository</li>
   *     <li>Word removed - if the word was successfully removed from the word repository</li>
   *     <li>Word not found - if the word was not found in the word repository</li>
   *     <li>You have already guessed this letter, try again! - if user has already guessed a letter</li>
   *     <li># <word> - if user has either lost or decided to quit game</li>
   *     <li>! - if right phrase has been guessed</li>
   *   </ul>
   * </p>
   *
   * @param socketInput     reader from client
   * @param socketOutput    writer to client
   * @param accountIn       reader from AccountService
   * @param accountOut      writer to AccountService
   * @param args            initial arguments passed in by user
   * @param wordServicePort port that WordService is running on
   * @param clientSocket    socket that is communicating with client
   * @throws IOException if any errors occur on either AccountService or WordService side
   */
  public static void serverPlay(BufferedReader socketInput,
      PrintWriter socketOutput,
      BufferedReader accountIn,
      PrintWriter accountOut,
      String[] args,
      int wordServicePort, Socket clientSocket) throws IOException {
    while (true) {
      socketOutput.println(
          "Start new game with the following command - start <number of words> <attempts>");
      String startRes = socketInput.readLine();
      if (startRes != null) {
        String[] startArgs = startRes.split(" ");

        // make UDP connection to word repository
        DatagramSocket wordRepository = new DatagramSocket();
        byte[] inputBuf = new byte[256];

        if (startArgs[0].equalsIgnoreCase("exit")) {
          accountOut.println("exit");

          break;
        } else if (startArgs[0].equalsIgnoreCase("start") && startArgs.length == 3) {
          String word = responseFromWordRepository(inputBuf, startRes, wordRepository,
              wordServicePort);

          int counter = Integer.parseInt(startArgs[1]) * Integer.parseInt(startArgs[2]);
          String display = word.replaceAll("[A-z]", "-");
          display = display + " C" + counter;
          String newDisplay = display;
          ArrayList<String> guessed = new ArrayList<>();
          boolean gameOver = false;
          socketOutput.println(
              newDisplay + " " + "Guess a letter of the phrase or guess the phrase:");
          System.out.println(word);
          while (!gameOver) {
            String message = "";

            if (!display.contains("-")) {
              break;
            }

            String playRes = socketInput.readLine();
            String[] playArgs = playRes.split(" ");

            // Check if word exists
            if (playRes.charAt(0) == '?') {
              if (playArgs.length == 2) {
                message = playArgs[1] + ": " + checkWord(inputBuf, playRes, wordRepository,
                    wordServicePort);
              } else {
                message = "error";
              }

              // Get score
            } else if (playRes.charAt(0) == '$') {
              message = checkScore(accountOut, accountIn, playArgs);

              // End game
            } else if (playRes.charAt(0) == '#') {
              message = "#" + word;
              socketOutput.println(message);
              return;
            } else if (playRes.charAt(0) == '!') {
              socketOutput.println("Starting new game...");
              break;
            } else if (playArgs[0].equalsIgnoreCase("+") || playArgs[0].equalsIgnoreCase("-")) {
              message = responseFromWordRepository(inputBuf, playRes, wordRepository,
                  wordServicePort);
            } else {
              // do a guess
              //if only a letter, guess letter
              if (playRes.length() == 1) {
                if (guessed.contains(playRes)) {
                  message = "You have already guessed this letter, try again!";
                  socketOutput.println(message);
                  continue;
                }
                newDisplay = "";
                for (int i = 0; i < word.length(); i++) {
                  if (word.charAt(i) == playRes.charAt(0)) {
                    newDisplay += playRes.charAt(0);
                  } else if (display.charAt(i) != '-') {
                    newDisplay += word.charAt(i);
                  } else {
                    newDisplay += "-";
                  }

                }
                if (!word.contains(playRes)) {
                  counter--;
                }
                guessed.add(playRes);
                newDisplay = newDisplay + " C" + counter;
                display = newDisplay;
                message = display;

                if (!newDisplay.contains("-")) {
                  socketOutput.println("!");
                  updateScore(accountIn, accountOut, args);
                  break;
                } else if (counter == 0) {

                  break;
                }
              } else {

                //if phrase is right, win game
                if (playRes.equalsIgnoreCase(word)) {
                  gameOver = true;
                  message = "!";
                  updateScore(accountIn, accountOut, args);

                  //if phrase is wrong, try again
                } else {
                  counter--;
                  if (counter != 0) {
                    message = "Incorrect, try again! C" + counter;
                  }
                }

              }
            }
            socketOutput.println(message);
          }
        } else if (startArgs[0].equalsIgnoreCase("$")) {
          String message = checkScore(accountOut, accountIn, startArgs);
          socketOutput.println(message);
        } else if (startArgs[0].equalsIgnoreCase("?")) {
          String message = checkWord(inputBuf, startRes, wordRepository, wordServicePort);
          socketOutput.println(message);
        } else if (startRes.equalsIgnoreCase("#")) {
          socketOutput.println("Exiting...");
          System.out.println("Client disconnected: " + clientSocket);
        } else if (startArgs[0].equalsIgnoreCase("+") || startArgs[0].equalsIgnoreCase("-")) {
          String message = responseFromWordRepository(inputBuf, startRes, wordRepository,
              wordServicePort);
          socketOutput.println(message);
        } else {
          socketOutput.println("Invalid command");
        }
      }
    }
  }

  /**
   * Same as the serverPlay method above, with the difference of handling the client-side requests
   * and communicating them with the server.
   *
   * @param socketIn  reader from server
   * @param stdin     commandline read from client
   * @param socketOut writer to server
   * @param username  user's username
   * @param password  user's password
   * @throws IOException if any errors occur while communicating to the server
   */
  public static void clientPlay(BufferedReader socketIn,
      BufferedReader stdin,
      PrintWriter socketOut,
      String username, String password) throws IOException {

    while (true) {
      System.out.println(socketIn.readLine());

      String res = stdin.readLine();
      String[] resArgs = res.split(" ");

      if (resArgs[0].equalsIgnoreCase("exit")) {
        socketOut.println("exit");
        return;
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
          System.out.println("What next? ");
        }

      } // Special commands without having to start a game
      else if (resArgs[0].equals("?")) {
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
}
