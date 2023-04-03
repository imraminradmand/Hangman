/**
 * A remote implementation of the WordServiceInterface that allows clients to retrieve phrases, add
 * and remove words from the word list, and check if a word is present in the list
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;
import java.nio.file.Path;
import java.nio.file.Paths;
public class WordService extends UnicastRemoteObject implements WordServiceInterface {

  static ArrayList<String> words = new ArrayList<>();

  protected WordService() throws RemoteException {
    super();
    populateWords();
  }

  void populateWords() throws RemoteException {
    try {

      File myObj = new File("A3/src/words.txt");
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        words.add(myReader.nextLine());
      }
    } catch (IOException e) {
      System.err.println("Error reading words.txt file: " + e.getMessage());
    }
  }

  /**
   * Returns a phrase given the number of words in the phrase
   *
   * @param numberOfWords - the number of words in the phrase
   * @return - the phrase
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public String getPhrase(int numberOfWords) throws RemoteException {
    List<String> phraseList = new ArrayList<>();
    StringJoiner phrase = new StringJoiner(" ");
    for (int i = 0; i < words.size(); i++) {
      if (phraseList.size() != numberOfWords) {
        int rand = (int) ((Math.random() * (words.size() - 1)) + 1);
        phraseList.add(words.get(rand));
      }
    }

    for (String s : phraseList) {
      phrase.add(s);
    }

    return phrase.toString().toLowerCase();
  }

  /**
   * Adds a word to the word repository
   *
   * @param word - the word to add
   * @return - true if the word was added, false if the word was already in the repository
   * @throws RemoteException - if the remote method call fails
   */
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

  /**
   * Removes a word from the word repository
   *
   * @param word - the word to remove
   * @return - true if the word was removed, false if the word was not in the repository
   * @throws RemoteException - if the remote method call fails
   */
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

  /**
   * Checks if a word is in the word repository
   *
   * @param word - the word to check
   * @return - true if the word is in the repository, false if the word is not in the repository
   * @throws RemoteException - if the remote method call fails
   */
  @Override
  public synchronized boolean checkWord(String word) throws RemoteException {
    return words.contains(word);
  }

  @Override
  public boolean isAlive() throws RemoteException {
    return true;
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
