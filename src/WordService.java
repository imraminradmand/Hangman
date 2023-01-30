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

    return phrase.toString();
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
    System.out.println("WordService is running...");
    while (true) {
      byte[] inputBuffer = new byte[256];
      byte[] outputBuffer;

      try {
        // Receive request
        DatagramPacket requestPacket = new DatagramPacket(inputBuffer, inputBuffer.length);
        socket.receive(requestPacket);

        InetAddress requestAddress = requestPacket.getAddress();
        int requestPort = requestPacket.getPort();

        // return phrase of given length;
        outputBuffer = getPhrase(Integer.parseInt(new String(requestPacket.getData(), 0, requestPacket.getLength()))).toString().getBytes();
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
