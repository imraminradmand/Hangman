import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * WordServiceHandler is a UDP server that provides a word repository for the game. It can return a
 * random word of a given length, a random phrase of a given length, add a word to the repository,
 * remove a word from the repository, and check if a word exists in the repository.
 *
 * @author Ramin Radmand, Tate Greeves, Emily Allerdings
 */
public class WordServiceHandler implements Runnable {

  private final DatagramSocket serverSocket;
  private final DatagramPacket receivePacket;
  private final ArrayList<String> words;

  public WordServiceHandler(DatagramSocket serverSocket, DatagramPacket receivePacket,
      ArrayList<String> words) {
    this.serverSocket = serverSocket;
    this.receivePacket = receivePacket;
    this.words = words;
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
      int rand = (int) ((Math.random() * (wordsOfRequestSize.size() - 1)) + 1);
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
    if (words.contains(word)) {
      res = "true";
    }

    return res;
  }

  @Override
  public void run() {
    try {
      byte[] outputBuffer = new byte[256];

      while (true) {
        try {

          InetAddress requestAddress = receivePacket.getAddress();
          int requestPort = receivePacket.getPort();

          String requestString = new String(receivePacket.getData(), 0, receivePacket.getLength());
          String[] requestArgs = requestString.split(" ");

          if (requestArgs[0].equalsIgnoreCase("start")) {
            outputBuffer = getPhrase(Integer.parseInt(requestArgs[1])).getBytes();
          } else if (requestArgs[0].equals("?")) {
            outputBuffer = wordExists(requestArgs[1]).getBytes();
          }

          DatagramPacket reply = new DatagramPacket(outputBuffer, outputBuffer.length,
              requestAddress, requestPort);
          serverSocket.send(reply);

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
  }
}
