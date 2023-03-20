/**
 * The GameObject class represents the game object that contains the details of a game being played
 * by a player. It contains the player's username, the number of words to be guessed, the number of
 * attempts remaining, and the word to be guessed. It also contains a list of letters that have been
 * guessed by the player.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

import java.util.ArrayList;

public class GameObject {

  private final String username;
  private final int numberOfWords;
  private int attempts;
  private String word;
  private ArrayList<Character> lettersGuess = new ArrayList<>();

  public GameObject(String username, int numberOfWords, int attempts, String word) {
    this.username = username;
    this.numberOfWords = numberOfWords;
    this.attempts = attempts;
    this.word = word.toLowerCase();
  }

  public ArrayList<Character> getLettersGuess() {
    return lettersGuess;
  }

  public void setLettersGuess(ArrayList<Character> lettersGuess) {
    this.lettersGuess = lettersGuess;
  }

  public String getUsername() {
    return username;
  }

  public int getNumberOfWords() {
    return numberOfWords;
  }

  public int getAttempts() {
    return attempts;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  /**
   * This function Stringifies the randomly chosen word so that it contains player guessed letters
   * along with attempts counter
   * Example: word => zebra
   * original response => -----C7 Stringified
   * response -> --br-C4
   *
   * @return stringified word
   */
  public String getStringifyedWord() {
    StringBuilder newWord = new StringBuilder();

    newWord.append("-".repeat(this.getWord().length()));
    newWord.append(" C").append(this.getAttempts());

    for (int x = 0; x < this.getWord().length(); x++) {
      for (char c : this.getLettersGuess()) {
        if (c == this.getWord().charAt(x)) {
          newWord.setCharAt(x, this.getWord().charAt(x));
        }
      }

      if (this.getWord().charAt(x) == ' ') {
        newWord.setCharAt(x, ' ');
      }
    }

    if (!newWord.toString().contains("-")) {
      return "You won, the phrase was: " + this.getWord()
          + "\nUsage: start <number of letters> <attempts>";
    }

    return newWord.toString();
  }

  public boolean guessLetter(char letter) {
    if (this.getLettersGuess().contains(letter)) {
      return false;
    }

    ArrayList<Character> newList = this.getLettersGuess();
    newList.add(letter);
    this.setLettersGuess(newList);

    if (!this.isLetterInWord(letter)) {
      this.setAttempts(this.getAttempts() - 1);
    }

    return true;
  }

  private boolean isLetterInWord(char letter) {
    for (char c : this.getWord().toCharArray()) {
      if (c == letter) {
        return true;
      }
    }
    return false;
  }

  public boolean alreadyGuessed(char letter) {
    return this.getLettersGuess().contains(letter);
  }
}
