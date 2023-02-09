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
