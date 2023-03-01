import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameServer {
    private static AccountInterface accountService;
    public static void main (String[] args) throws IOException, NotBoundException {
        /*
            Account service stub which includes two separate calls.

            readFromFile - which take in the arguments as a String array
                           arg[0] = "username"
                           arg[1] = "password"

                           The returning result is a String that returns the relation, in other word's username, password and score
                           delimited by spaces or if the account does not exist it will be !noaccount!.

            writeToFile - which takes in the arguments as a String array
                          arg[0] = "username"
                          arg[1] = "password"
                          arg[2] = "score"

                          The returning result is a boolean, this is whether the write was successful.
         */
        accountService = (AccountInterface) Naming.lookup("rmi://localhost:4777" + "/AccountService");

        startGameServer();
    }

    private static void startGameServer() throws RemoteException {
        /*
        * Server Logic - game logic should be modulated into its own function that takes in some sort of
        *                player identifier and executes it in its own state.
        *
        *
        * */
        boolean running = true;

        while(running){

        }
    }
}
