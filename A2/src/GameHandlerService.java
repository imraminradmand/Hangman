import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameHandlerService extends UnicastRemoteObject implements GameHandlerInterface {
    protected GameHandlerService() throws RemoteException {
        super();
    }

    @Override
    public String startGame(String player, int number_of_words, int failed_attempt_factor) throws RemoteException {
        return null;
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
