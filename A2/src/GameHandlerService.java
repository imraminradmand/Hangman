import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;

public class GameHandlerService extends UnicastRemoteObject implements GameHandlerInterface {
    private static final ArrayList<GameObject> gameStates = new ArrayList<>();
    private static AccountInterface accountService;

    protected GameHandlerService() throws RemoteException {
        super();
        try {
            accountService = (AccountInterface) Naming.lookup("rmi://localhost:4777" + "/AccountService");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String startGame(String player, int number_of_words, int failed_attempt_factor) throws RemoteException {


        gameStates.add( new GameObject(player, number_of_words, failed_attempt_factor, "replace with random word"));

        return Objects.requireNonNull(getPlayerState(player)).getStringifyedWord();
    }

    @Override
    public String guessLetter(String player, char letter) throws RemoteException {
        GameObject gameState = getPlayerState(player);

        assert gameState != null;
        if(gameState.guessLetter(Character.toLowerCase(letter))){
            return gameState.getStringifyedWord();
        }
        return "Incorrect guess!\n" + gameState.getStringifyedWord();
    }

    @Override
    public String guessPhrase(String player, String phrase) throws RemoteException {
        GameObject gameState = getPlayerState(player);
        assert gameState != null;
        if (phrase.toLowerCase().equals(gameState.getWord())){
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
        String response = "";
        GameObject gameState = getPlayerState(player);
        assert gameState != null;
        response += "Restarting game, the correct word was: " + gameState.getWord();
        removeGameState(player);

        return response;
    }

    @Override
    public String addWord() throws RemoteException {
        return null;
    }

    @Override
    public String removeWord() throws RemoteException {
        return null;
    }

    @Override
    public String checkWord() throws RemoteException {
        return null;
    }

    @Override
    public boolean login(String username, String password) throws RemoteException{
        if(getPlayerState(username) != null){
            return false;
        }

        return !accountService.readFromFile(username, password).equals("!noaccount!");
    }

    @Override
    public boolean register(String username, String password) throws RemoteException{
        if (!accountService.readFromFile(username, password).equals("!noaccount!")){
            return false;
        }
        try {
            accountService.writeToFile(username, password, "0");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
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
    }
}
