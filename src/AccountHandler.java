import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class AccountHandler implements Runnable{
    private Socket socket;
    public AccountHandler(Socket socket){
        this.socket = socket;
    }


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
                        socketOutput.close();
                        socketInput.close();
                        socket.close();
                        return;
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
