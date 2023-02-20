import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * WordService is a UDP server that provides a word repository for the game. It can return a random
 * word of a given length, a random phrase of a given length, add a word to the repository, remove a
 * word from the repository, and check if a word exists in the repository.
 * <p>
 * The WordService is started by running the main method of this class. The port number is passed as
 * a command line argument. The port number must be between 1024 and 65535. The word repository is
 * initialized with the words in the file src/resources/words.txt.
 * </p>
 *
 * @author Ramin Radmand, Tate Greeves, Emily Allerdings
 */
public class WordService {

  private static final ArrayList<String> words = new ArrayList<>();
  private final DatagramSocket socket;
  private static final String USAGE = "java WordService [port]";

  /**
   * Constructs a new WordService object that listens on the specified port.
   *
   * @param port the port number to listen on
   * @throws SocketException if there is an error in the underlying protocol
   */
  public WordService(int port) throws SocketException {
    socket = new DatagramSocket(port);
  }

  /**
   * Writes the list of words to a text file, "words.txt". If an IOException is
   * thrown, a message is printed to the error stream.
   *
   * @throws IOException if an error occurs while writing the file
   */
  private void writeOut() {
    try (FileWriter writer = new FileWriter("../words.txt")) {
      for (String str : words) {
        writer.write(str + System.lineSeparator());
      }
      writer.flush();
    } catch (IOException e) {
      System.err.println("Error writing words.txt file: " + e.getMessage());
    }
  }

  /**
   * Gets (a) phrase(s) from the words ArrayList given a number.
   *
   * @param numPhrase number of phrases
   * @return String of phrases separated by a space
   */
  private String getPhrase(int numPhrase) {
    List<String> phraseList = new ArrayList<>();
    StringJoiner phrase = new StringJoiner(" ");
    for (int i = 0; i < words.size(); i++) {
      if (phraseList.size() != numPhrase) {
        int rand = (int) ((Math.random() * (words.size() - 1)) + 1);
        phraseList.add(words.get(rand));
      }
    }

    for (String s : phraseList) {
      phrase.add(s);
    }

    return phrase.toString().toLowerCase();
  }

  private synchronized String addWord(String word) {
    if (wordExists(word).equals("true")) {
      return "word exists";
    }

    words.add(word);
    writeOut();
    return "word added";
  }

  /**
   * Given a word it will attempt to remove it from the words ArrayList, and re-write to the words
   * file.
   *
   * @param word word to remove
   * @return message
   */
  private synchronized String removeWord(String word) {
    if (wordExists(word).equals("true")) {
      words.remove(word);
      writeOut();
      return "word removed";
    }
    return "word does not exist";
  }

  /**
   * Given a word it will attempt to add it to the words ArrayList, and re-write to the words file.
   *
   * @param word word to add
   * @return message
   */
  private String wordExists(String word) {
    String res = "false";
    if (words.contains(word)) {
      res = "true";
    }

    return res;
  }

  /**
   * Reads the words.txt file, and initializes the words ArrayList that is used for the WordService
   * functionality.
   */
  private static void initializeArrayList() {
    try {
      File myObj = new File("../words.txt");
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        words.add(myReader.nextLine());
      }
    } catch (IOException e) {
      System.err.println("Error reading words.txt file: " + e.getMessage());
    }
  }

  /**
   * The serve method serves the WordService by listening for incoming requests on the socket and
   * responding to the requests. It uses a DatagramSocket to receive incoming requests as
   * DatagramPackets and sends responses back to the client as DatagramPackets.
   * <p>Incoming requests are first parsed into request strings and the arguments are extracted.
   * The first argument of the request string determines the type of request:
   * <ul>
   * <li>"start": prompts to start the game
   * <li>"?": checks if the word specified in the second argument of the request string exists.
   * <li>"+": adds the word specified in the second argument of the request string to the word list.
   * <li>"-": removes the word specified in the second argument of the request string from the word list.
   * </ul>
   *
   * @throws IOException                    if an I/O error occurs when using the socket for
   *                                        communication.
   * @throws ArrayIndexOutOfBoundsException if not enough arguments are provided in the request.
   */
  private void serve() {
    System.out.println("WordService is running...");
    while (true) {
      byte[] inputBuffer = new byte[256];
      byte[] outputBuffer = new byte[256];

      try {
        // Receive request
        DatagramPacket requestPacket = new DatagramPacket(inputBuffer, inputBuffer.length);
        socket.receive(requestPacket);

        InetAddress requestAddress = requestPacket.getAddress();
        int requestPort = requestPacket.getPort();

        String requestString = new String(requestPacket.getData(), 0, requestPacket.getLength());
        String[] requestArgs = requestString.split(" ");

        if (requestArgs[0].equalsIgnoreCase("start")) {
          // return phrase of given length;
          outputBuffer = getPhrase(Integer.parseInt(requestArgs[1])).getBytes();
        } else if (requestArgs[0].equals("?")) {
          System.out.println("word exists: " + wordExists(requestArgs[1]));
          outputBuffer = wordExists(requestArgs[1]).getBytes();
        } else if (requestArgs[0].equalsIgnoreCase("+")) {
          outputBuffer = addWord(requestArgs[1]).getBytes();
        } else if (requestArgs[0].equalsIgnoreCase("-")) {
          outputBuffer = removeWord(requestArgs[1]).getBytes();
        }

        DatagramPacket reply = new DatagramPacket(outputBuffer, outputBuffer.length, requestAddress,
            requestPort);
        socket.send(reply);

      } catch (IOException e) {
        System.err.println(
            "An error occurred while receiving or sending a packet: " + e.getMessage());
      } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println("Not enough arguments were provided in the request: " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println(USAGE);
      System.exit(1);
    }

    int port = 0;

    initializeArrayList();
    WordService wordService;

    try {
      port = Integer.parseInt(args[0]);
      wordService = new WordService(port);
    } catch (SocketException e) {
      System.err.println(
          "Failed to start WordService on port " + port + " due to: " + e.getMessage());
      throw new RuntimeException("Failed to start WordService", e);
    }

    wordService.serve();
    wordService.socket.close();
  }
}
