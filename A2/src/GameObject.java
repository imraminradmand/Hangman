import java.util.ArrayList;

public class GameObject {
    private final String username;
    private final int numberOfWords;
    private int attempts;
    private String word;
    private ArrayList<Character> lettersGuess;

    public GameObject (String username, int numberOfWords, int attempts, String word){
        this.username = username;
        this.numberOfWords = numberOfWords;
        this.attempts = attempts;
        this.word = word;
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

    public String getStringifyedWord(){
        StringBuilder newWord = new StringBuilder();

        for(int x = 0; x < this.getNumberOfWords(); x++){
            newWord.append("-");
        }
        newWord.append("C").append(this.getAttempts());

        for (int x = 0; x < this.getNumberOfWords(); x++){
            for (char c : this.getLettersGuess()){
                if (c == this.getWord().charAt(x)){
                    newWord.setCharAt(x, this.getWord().charAt(x));
                }
            }
        }

        return newWord.toString();
    }
}
