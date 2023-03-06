import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GameHandlerService extends UnicastRemoteObject implements GameHandlerInterface {
    private static final ArrayList<GameObject> gameStates = new ArrayList<>();
    private static final Set<String> loggedInUsers = new HashSet<>();
    private static AccountInterface accountService;
    private static WordServiceInterface wordService;

    protected GameHandlerService() throws RemoteException {
        super();
        try {
            accountService = (AccountInterface) Naming.lookup("rmi://localhost:4777" + "/AccountService");
            wordService = (WordServiceInterface) Naming.lookup("rmi://localhost:4777" + "/WordService");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String startGame(String player, int number_of_words, int failed_attempt_factor) throws RemoteException {
        String randomWord = wordService.getPhrase(number_of_words);
        System.out.println(randomWord);
        gameStates.add( new GameObject(player, number_of_words, failed_attempt_factor, randomWord));
        return Objects.requireNonNull(getPlayerState(player)).getStringifyedWord();
    }

    @Override
    public String guessLetter(String player, char letter) throws RemoteException {
        GameObject gameState = getPlayerState(player);

        assert gameState != null;
        if(gameState.guessLetter(Character.toLowerCase(letter))){
            return gameState.getStringifyedWord();
        } else if (gameState.alreadyGuessed(Character.toLowerCase(letter))) {
            return "Letter already guessed\n" + gameState.getStringifyedWord();
        }
        return "Incorrect guess!\n" + gameState.getStringifyedWord();
    }

    @Override
    public String guessPhrase(String player, String phrase) throws IOException {
        GameObject gameState = getPlayerState(player);
        assert gameState != null;
        if (phrase.toLowerCase().equals(gameState.getWord())){
            accountService.updateScore(gameState.getUsername(), 100);
            removeGameState(player);
            return "You guessed the correct phrase.\nUsage: start <number of letters> <attempts>";
        }

        return null;
    }

    @Override
    public String endGame(String player) throws RemoteException {
        GameObject gameState = getPlayerState(player);
        removeGameState(player);

        assert gameState != null;
        return "Game ended, the word was: " + gameState.getWord();
    }

    @Override
    public String restartGame(String player) throws RemoteException {
        System.out.println(player);
        String response = "";
        GameObject gameState = getPlayerState(player);
        System.out.println(gameState);
        assert gameState != null;
        response += "Restarting game, the correct word was: " + gameState.getWord();
        removeGameState(player);

        return response;
    }

    @Override
    public boolean addWord(String word) throws RemoteException {
        return wordService.addWord(word);
    }

    @Override
    public boolean removeWord(String word) throws RemoteException {
        return wordService.removeWord(word);
    }

    @Override
    public boolean checkWord(String word) throws RemoteException {
        return wordService.checkWord(word);
    }

    @Override
    public boolean login(String username, String password) throws RemoteException{

        if(getPlayerState(username) != null || loggedInUsers.contains(username)){
            return false;
        }
        loggedInUsers.add(username);
        return !accountService.readFromFile(username, password).equals("!noaccount!");
    }

    @Override
    public boolean register(String username, String password) throws RemoteException{
        if (!accountService.readFromFile(username, password).equals("!noaccount!")){
            return false;
        }
        try {
            accountService.writeToFile(username, password, "0");
            loggedInUsers.add(username);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void logOut(String username) throws RemoteException {
       loggedInUsers.remove(username);
    }

    @Override
    public String getScore(String username, String password) throws RemoteException {
        return "Your high score is " + accountService.readFromFile(username, password);
    }

    private GameObject getPlayerState(String username){
        for (GameObject obj : gameStates){
            if(obj.getUsername().equals(username)){
                return obj;
            }
        }
        return null;
    }

    private void removeGameState(String username){
        gameStates.removeIf(obj -> obj.getUsername().equals(username));
        loggedInUsers.remove(username);
    }

}
