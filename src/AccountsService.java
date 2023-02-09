import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountsService {

    /*
    Main function:

    The goal of this function is to run the server that fetched from the users.txt file on the machine.

    This implementation is RESTful, in other words using GET and POST methods.

    The protocol that is implemented follows as:
    GET <username> <password> - The return statement pass through the socket will either be the user's score or if the account does not exist
                                it will return !noaccount!
    POST <username> <password> <score> - This will return !success! if it is successful, if it fails there is no output.

    Because this implementation is a single threaded TCP server, there is no need for synchronization.
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket;

        File file = new File("users.txt");
        file.createNewFile();

        try {

            serverSocket = new ServerSocket(7777);

            System.out.println("Server is running...");
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

            while (true) {
               fixedThreadPool.execute(new AccountHandler(serverSocket.accept()));
            }
        } catch (IOException e) {
            System.out.println(
                    "Exception caught when trying to listen on port " + 7777 + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

}
