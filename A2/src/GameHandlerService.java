import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameHandlerService extends UnicastRemoteObject implements GameHandlerInterface {
    private GameObject gameState;
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

        gameState = new GameObject(player, number_of_words, failed_attempt_factor, "replace with random word");

        return gameState.getStringifyedWord();
    }

    @Override
    public String guessLetter(String player, char letter) throws RemoteException {
        return null;
    }

    @Override
    public String guessPhrase(String player, String phrase) throws RemoteException {
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
}
