import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;

public class GameHandlerService extends UnicastRemoteObject implements GameHandlerInterface {
    private ArrayList<GameObject> gameStates;
    private AccountInterface accountService;

    protected GameHandlerService() throws RemoteException {
        super();
    }

    @Override
    public String startGame(String player, int number_of_words, int failed_attempt_factor) throws RemoteException {
        try {
            accountService = (AccountInterface) Naming.lookup("rmi://localhost:4777" + "/AccountService");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

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
        return null;
    }

    @Override
    public String restartGame(String player) throws RemoteException {
        return null;
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
        return false;
    }

    @Override
    public boolean register(String username, String password) throws RemoteException{
        return false;
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
