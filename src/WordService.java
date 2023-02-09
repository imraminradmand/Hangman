import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * WordService is a UDP server that provides a word repository for the game.
 * It can return a random word of a given length, a random phrase of a given
 * length, add a word to the repository, remove a word from the repository,
 * and check if a word exists in the repository.
 * <p>
 *   The WordService is started by running the main method of this class.
 *   The port number is passed as a command line argument.
 *   The port number must be between 1024 and 65535.
 *   The word repository is initialized with the words in the file
 *   src/resources/words.txt.
 * </p>
 * @author Ramin Radmand, Tate Greeves, Emily Allerdings
 */
public class WordService {
  private static final ArrayList<String> words = new ArrayList<>();
  private final DatagramSocket socket;
  private static final int PORT = 5599;

  public WordService(int port) throws SocketException {
    socket = new DatagramSocket(port);
  }

  private void writeOut() {
    try (FileWriter writer = new FileWriter("src/resources/words.txt")) {
      for (String str : words) {
        writer.write(str + System.lineSeparator());
      }
      writer.flush();
    } catch (IOException e) {
      System.err.println("Error writing words.txt file: " + e.getMessage());
    }
  }

  private String getPhrase(int length) {
    List<String> phraseList = new ArrayList<>();
    StringJoiner phrase = new StringJoiner(" ");
    for (int i = 0; i < words.size(); i++) {
      if (phraseList.size() != length) {
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

  private synchronized String removeWord(String word) {
    if (wordExists(word).equals("true")) {
      words.remove(word);
      writeOut();
      return "word removed";
    }
    return "word does not exist";
  }

  private String wordExists(String word) {
    String res = "false";
    if (words.contains(word)) res = "true";

    return res;
  }
  private static void initializeArrayList() {
    try (BufferedReader br = new BufferedReader(new FileReader("src/resources/words.txt"))) {
      String line;
      while ((line = br.readLine()) != null) {
        words.add(line);
      }
    } catch (IOException e) {
      System.err.println("Error reading words.txt file: " + e.getMessage());
    }
  }

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

        DatagramPacket reply = new DatagramPacket(outputBuffer, outputBuffer.length, requestAddress, requestPort);
        socket.send(reply);

      } catch (IOException e) {
        System.err.println("An error occurred while receiving or sending a packet: " + e.getMessage());
      } catch (NumberFormatException e) {
        System.err.println("Invalid input for the length of the phrase: " + e.getMessage());
      } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println("Not enough arguments were provided in the request: " + e.getMessage());
      }
    }
  }
  public static void main(String[] args) {
    initializeArrayList();
    WordService wordService;

    try {
      wordService = new WordService(PORT);
    } catch (SocketException e) {
      System.err.println("Failed to start WordService on port " + PORT + " due to: " + e.getMessage());
      throw new RuntimeException("Failed to start WordService", e);
    }

    wordService.serve();
    wordService.socket.close();
  }
}
