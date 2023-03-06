import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

public class WordService extends UnicastRemoteObject implements WordServiceInterface {

  static ArrayList<String> words = new ArrayList<>();

  protected WordService() throws RemoteException {
    super();
    populateWords();
  }

  void populateWords() throws RemoteException {
    try {
      File myObj = new File("words.txt");
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        words.add(myReader.nextLine());
      }
    } catch (IOException e) {
      System.err.println("Error reading words.txt file: " + e.getMessage());
    }
  }

  @Override
  public String getPhrase(int length) throws RemoteException {
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

  @Override
  public synchronized boolean addWord(String word) throws RemoteException {
    if (!words.contains(word)) {
      words.add(word);
      writeOut();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public synchronized boolean removeWord(String word) throws RemoteException {
    if (words.contains(word)) {
      words.remove(word);
      writeOut();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public synchronized boolean checkWord(String word) throws RemoteException {
    return words.contains(word);
  }

  private void writeOut() throws RemoteException {
    try (FileWriter writer = new FileWriter("words.txt")) {
      for (String str : words) {
        writer.write(str + System.lineSeparator());
      }
      writer.flush();
    } catch (IOException e) {
      System.err.println("Error writing words.txt file: " + e.getMessage());
    }
  }
}
