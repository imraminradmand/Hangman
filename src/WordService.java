import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordService {

  private static final int PORT = 5599;
  private static final int POOL_SIZE = 10;
  private static final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
  private static final ArrayList<String> words = new ArrayList<>();


  public static void main(String[] args) {
    initializeArrayList();
    try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
      System.out.println("WordService is running...");
      while (true) {
        byte[] receiveData = new byte[256];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        threadPool.submit(new WordServiceHandler(serverSocket, receivePacket, words));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
}
