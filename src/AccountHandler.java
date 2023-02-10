import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * AccountHandler is a class that implements the Runnable interface and is used to handle server
 * connections to the AccountService. It listens to the server and provides the required response.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */
public class AccountHandler implements Runnable {

  private final Socket socket;

  /**
   * Constructor for ClientHandler class.
   *
   * @param socket Socket to communicate with the AccountService.
   */
  public AccountHandler(Socket socket) {
    this.socket = socket;
  }

  /**
   * The goal of this function is to handle the server requests by utilizing file IO.
   * The protocol that is implemented follows as:
   * GET <username> <password> - The return statement pass through the socket
   * will either be the user's score or if the account does not exist it will return !noaccount!
   * POST <username> <password> <score> - This will return !success! if it
   * is successful, if it fails there is no output.
   */
  @Override
  public void run() {
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

          if (clientArgs[0].equals("exit")) {
            break;
          }

          socketOutput.println(fileIO(clientArgs));
        }
      }
      socketOutput.close();
      socketInput.close();
      socket.close();
    } catch (IOException e) {
      System.err.println(
          "Error occurred while reading from or writing to the socket: " + e.getMessage());
      try {
        socket.close();
      } catch (IOException closeException) {
        System.err.println(
            "Error occurred while trying to close the socket: " + closeException.getMessage());
      }
    }
  }

  /**
   * Purpose: The main function that controls thread synchronization
   * for file IO (makes sure that only one thread can write or read at once).
   * */
  private synchronized String fileIO(String[] clientArgs) throws IOException {
    if (clientArgs[0].equalsIgnoreCase("get")) {
      return readFromFile(clientArgs);
    } else {
      return writeToFile(clientArgs);
    }
  }

  /**
   * Purpose: The goal of the method is to provide capability to read from the file.
   * This will return the data for a specified user account, or it will return an invalid account.
   * */
  private String readFromFile(String[] clientArgs) throws FileNotFoundException {
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
    myReader.close();
    return result;
  }

  /**
   * GOAL: The purpose of this file is to write an account and score to the file. The file
   * is copied, deleted and recreated with the contents plus the specified client arguments.
   * */
  private String writeToFile(String[] clientArgs) throws IOException {
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

      return ("!success!");
    } else {
      return ("!fail!");
    }
  }
}
