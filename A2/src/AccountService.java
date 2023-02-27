import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class AccountService extends UnicastRemoteObject implements AccountInterface {
    protected AccountService() throws RemoteException {
        super();
    }

    @Override
    public synchronized boolean writeToFile(String[] clientArgs) throws IOException {
        File file = new File("../users.txt");
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

            FileWriter writer = new FileWriter("../users.txt");
            lines.add(clientArgs[1] + " " + clientArgs[2] + " " + clientArgs[3]);

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
    public synchronized String readFromFile(String[] clientArgs) {
        File myObj = new File("../users.txt");
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

            if (arg[0].equals(clientArgs[1]) && arg[1].equals(clientArgs[2])) {
                result = data;
                break;
            }
        }
        myReader.close();
        return result;
    }
}
