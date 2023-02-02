import java.io.BufferedReader;
import java.io.FileReader;
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

  public WordService(int port) throws SocketException {
    socket = new DatagramSocket(port);
  }
  private String getRandomWord(int requestedLength) {
    String res = "";
    List<String> wordsOfRequestSize = new ArrayList<>();

    for (String word : words) {
      if (word.length() == requestedLength) {
        wordsOfRequestSize.add(word);
      }
    }

    for (int i = 0; i < wordsOfRequestSize.size(); i++) {
      int rand = (int)((Math.random() * (wordsOfRequestSize.size() - 1)) + 1);
      res = wordsOfRequestSize.get(rand).toLowerCase();
    }
    return res;
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

  private void addWord(String word) {
    words.add(word);
  }

  private void removeWord(String word) {
    words.remove(word);
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
      e.printStackTrace();
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
        }

        DatagramPacket reply = new DatagramPacket(outputBuffer, outputBuffer.length, requestAddress, requestPort);
        socket.send(reply);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  public static void main(String[] args) {
//    if (args.length != 1) {
//      System.exit(1);
//    }

    initializeArrayList();

//    int port = 0;
    WordService wordService;

    try {
//      port = Integer.parseInt(args[0]);
      wordService = new WordService(5599);
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }

    wordService.serve();
    wordService.socket.close();
  }
}
