import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

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
        if (args.length != 1) {
            System.exit(1);
        }

        int port = 0;
        ServerSocket serverSocket;

        try {
            port = Integer.parseInt(args[0]);
            serverSocket = new ServerSocket(port);

            System.out.println("Server is running...");

            while (true) {
                Socket socket = serverSocket.accept();

                InputStreamReader socketInputReader = new InputStreamReader(socket.getInputStream());
                OutputStream socketOutStream = socket.getOutputStream();

                BufferedReader socketInput = new BufferedReader(socketInputReader);
                PrintWriter socketOutput = new PrintWriter(socketOutStream, true);

                while (true) {
                    try {
                        String clientResponse = socketInput.readLine();
                        System.out.println(clientResponse);
                        if (clientResponse != null) {
                            String[] clientArgs = clientResponse.split(" ");

                            File myObj = new File("users.txt");
                            Scanner myReader = new Scanner(myObj);
                            if (clientArgs[0].equalsIgnoreCase("get")) {
                                String result = "!noaccount!";
                                while (myReader.hasNextLine()) {
                                    String data = myReader.nextLine();
                                    String[] arg = data.split(" ");

                                    if (arg[0].equals(clientArgs[1]) && arg[1].equals(clientArgs[2])) {
                                        result = data;
                                        break;
                                    }
                                }
                                socketOutput.println(result);
                                myReader.close();
                            } else if (clientArgs[0].equalsIgnoreCase("post")) {
                                ArrayList<String> lines = new ArrayList<>();
                                while (myReader.hasNextLine()) {
                                    String data = myReader.nextLine();
                                    lines.add(data);
                                }
                                myReader.close();

                                for (String line : lines) {
                                    String[] arg = line.split(" ");
                                    if (arg[0].equals(clientArgs[1])) {
                                        lines.remove(line);
                                        break;
                                    }
                                }

                                File file = new File("users.txt");

                                if (file.delete()) {
                                    file.createNewFile();
                                }

                                FileWriter writer = new FileWriter("users.txt");
                                lines.add(clientArgs[1]  + " " + clientArgs[2]  + " " + clientArgs[3]);

                                for (String s : lines) {
                                    writer.write(s + '\n');
                                }
                                writer.close();
                                socketOutput.println("!success!");
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                //socketInput.close();
                //socketOutput.close();
                //socket.close();
            }
        } catch (IOException e) {
            System.out.println(
                    "Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

}
