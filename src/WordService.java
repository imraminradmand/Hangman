import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

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

  private void addWord(String word) {
    words.add(word);
  }

  private void removeWord(String word) {
    words.remove(word);
  }

  private boolean wordExists(String word) {
    return words.contains(word);
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
    while (true) {
      try {
        System.out.println("Listening for requests...");
        byte[] inputBuffer = new byte[256];
        byte[] outputBuffer;

        // Receive request
        DatagramPacket requestPacket = new DatagramPacket(inputBuffer, inputBuffer.length);
        socket.receive(requestPacket);
        InetAddress requestAddress = requestPacket.getAddress();
        int requestPort = requestPacket.getPort();

        // return random word for now - TESTING
        outputBuffer = getRandomWord(Integer.parseInt(new String(requestPacket.getData()).trim())).getBytes();
        DatagramPacket reply = new DatagramPacket(outputBuffer, outputBuffer.length, requestAddress, requestPort);
        socket.send(reply);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  public static void main(String[] args) {
    if (args.length != 1) {
      System.exit(1);
    }

    initializeArrayList();

    int port = 0;
    WordService wordService;

    try {
      port = Integer.parseInt(args[0]);
      wordService = new WordService(port);
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }

    wordService.serve();
    wordService.socket.close();
  }
}
