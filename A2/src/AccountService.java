import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class AccountService extends UnicastRemoteObject implements AccountInterface{
    protected AccountService() throws RemoteException {
        super();
    }

    @Override
    public synchronized boolean writeToFile(String username, String password, String score) throws IOException, RemoteException {
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
            if (arg[0].equals(username)) {
                lines.remove(line);
                break;
            }
        }

        if (file.delete()) {
            file.createNewFile();

            FileWriter writer = new FileWriter("users.txt");
            lines.add(username + " " + password + " " + score);

            for (String s : lines) {
                writer.write(s + '\n');
            }
            writer.close();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized String readFromFile(String username, String password)  throws RemoteException{
        File myObj = new File("users.txt");
        Scanner myReader = null;
        try {
            myReader = new Scanner(myObj);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String result = "!noaccount!";
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] arg = data.split(" ");

            if (arg[0].equals(username) && arg[1].equals(password)) {
                result = data.split(" ")[2];
                break;
            }
        }
        myReader.close();
        return result;
    }

    @Override
    public void updateScore(String username, int score) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(" ");

                if (fields[0].equals(username)) {
                    fields[2] = Integer.toString(Integer.parseInt(fields[2]) + score);
                    line = String.join(" ", fields);
                }

                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt"))) {
            writer.write(sb.toString());
        }
    }
}
