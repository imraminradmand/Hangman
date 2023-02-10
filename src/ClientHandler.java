import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class handles the client connection. It is instantiated by the server and runs in a separate
 * thread. It is responsible for reading the client's requests and sending the appropriate
 * responses. It also handles the communication with the word repository and the account service.
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
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */
public class ClientHandler implements Runnable {

  private final Socket clientSocket;
  private final int WORD_SERVICE_PORT;
  private final int ACCOUNT_SERVICE_PORT;

  ClientHandler(Socket socket, int wordServicePort, int accountServicePort) {
    this.clientSocket = socket;
    this.WORD_SERVICE_PORT = wordServicePort;
    this.ACCOUNT_SERVICE_PORT = accountServicePort;
  }

  private String checkWord(byte[] buf,
      byte[] inputBuf,
      String input,
      DatagramSocket wordRepository) throws IOException {

    String response = responseFromWordRepository(buf, inputBuf, input, wordRepository);
    String exists = "False";
    if (response.equalsIgnoreCase("true")) {
      exists = "True";
    }

    return exists;
  }

  private String checkScore(PrintWriter accountOut, BufferedReader accountIn, String[] args)
      throws IOException {

    accountOut.println("get " + args[1] + " " + args[2]);

    String[] result = accountIn.readLine().split(" ");
    if (result.length > 1) {
      return ("High-score for " + result[0] + " is " + result[2]);
    }
    return "No highscore";
  }

  private String responseFromWordRepository(byte[] buf,
      byte[] inputBuf,
      String input,
      DatagramSocket wordRepository) throws IOException {
    buf = input.getBytes();

    InetAddress address = InetAddress.getByName("localhost");
    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, WORD_SERVICE_PORT);
    wordRepository.send(packet);

    DatagramPacket wordRepoReply = new DatagramPacket(inputBuf, inputBuf.length, address,
        WORD_SERVICE_PORT);
    wordRepository.receive(wordRepoReply);

    return new String(wordRepoReply.getData(), 0, wordRepoReply.getLength());
  }

  private void play(BufferedReader socketInput,
      PrintWriter socketOutput,
      BufferedReader accountIn,
      PrintWriter accountOut,
      String[] args) throws IOException {
    while (true) {
      socketOutput.println(
          "Start new game with the following command - start <number of words> <attempts>");
      String startRes = socketInput.readLine();
      if (startRes != null) {
        String[] startArgs = startRes.split(" ");

        // make UDP connection to word repository
        DatagramSocket wordRepository = new DatagramSocket();
        byte[] buf = new byte[256];
        byte[] inputBuf = new byte[256];

        if (startArgs[0].equalsIgnoreCase("exit")) {
          accountOut.println("exit");

          break;
        } else if (startArgs[0].equalsIgnoreCase("start") && startArgs.length == 3) {
          String word = responseFromWordRepository(buf, inputBuf, startRes, wordRepository);

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
                message = playArgs[1] + ": " + checkWord(buf, inputBuf, playRes, wordRepository);
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
              message = responseFromWordRepository(buf, inputBuf, playRes, wordRepository);
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
          String message = checkWord(buf, inputBuf, startRes, wordRepository);
          socketOutput.println(message);
        } else if (startRes.equalsIgnoreCase("#")) {
          socketOutput.println("Exiting...");
          System.out.println("Client disconnected: " + clientSocket);
        } else if (startArgs[0].equalsIgnoreCase("+") || startArgs[0].equalsIgnoreCase("-")) {
          String message = responseFromWordRepository(buf, inputBuf, startRes, wordRepository);
          socketOutput.println(message);
        } else {
          socketOutput.println("Invalid command");
        }
      }
    }
  }

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
                play(socketInput, socketOutput, accountIn, accountOut, args);
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
                  play(socketInput, socketOutput, accountIn, accountOut, args);
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
