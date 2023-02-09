import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class AccountHandler implements Runnable{
    private Socket socket;
    public AccountHandler(Socket socket){
        this.socket = socket;
    }


    /*
    * Main function
    *
    * In this function we use the synchronized keyword. The solution may seem that it contradicts the point of parallelization, but sockets aren't thread safe when
    * they are shared with different threads. In this function we do file IO which requires thread synchronization, thus by synchronizing the function we are allowing
    * a single thread to read/write to the file at once.
    *
    * The reasoning we use the thread pool is that it allows the server to stay open with the account server in a one-to-one relationship as long as the client is connected.
    * Once the client disconnects the server is automatically shutdown and the thread is released back to the thread pool to handle another client.
    *
    *
    * The communication protocol is relatively straightforward and is outlined in the AccountsService file.
    * */
    @Override
    public synchronized void run() {
        try {
            InputStreamReader socketInputReader = new InputStreamReader(socket.getInputStream());
            OutputStream socketOutStream = socket.getOutputStream();

            BufferedReader socketInput = new BufferedReader(socketInputReader);
            PrintWriter socketOutput = new PrintWriter(socketOutStream, true);

            while (true) {
                String clientResponse = socketInput.readLine();
                System.out.println(clientResponse);
                if (clientResponse != null) {
                    String[] clientArgs = clientResponse.split(" ");

                    if (clientArgs[0].equals("exit")){
                        break;
                    }

                    if (clientArgs[0].equalsIgnoreCase("get")) {
                        File myObj = new File("users.txt");
                        Scanner myReader = new Scanner(myObj);
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
                        File file = new File("users.txt");
                        Scanner myReader = new Scanner(file);
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

                        if (file.delete()) {
                            file.createNewFile();


                            FileWriter writer = new FileWriter("users.txt");
                            lines.add(clientArgs[1] + " " + clientArgs[2] + " " + clientArgs[3]);

                            for (String s : lines) {
                                writer.write(s + '\n');
                            }
                            writer.close();
                            socketOutput.println("!success!");
                        } else {
                            socketOutput.println("!fail!");
                        }
                    }
                }
            }
            socketOutput.close();
            socketInput.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
