/**
 * The AccountService class implements the AccountInterface and provides methods to read and write
 * user data to a file, as well as update the score of a user.
 *
 * @author Tate Greeves, Ramin Radmand, Emily Allerdings
 */

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

public class AccountService extends UnicastRemoteObject implements AccountInterface {

  private static final int SCORE_INCREMENT = 100;

  protected AccountService() throws RemoteException {
    super();
  }

  /**
   * This method writes the user's username, password, and score to a file.
   *
   * @param username The user's username.
   * @param password The user's password.
   * @param score    The user's score.
   * @return True if the user's data was successfully written to the file, false otherwise.
   * @throws IOException If there is an error writing to the file.
   */
  @Override
  public synchronized boolean writeToFile(String username, String password, String score)
      throws IOException {
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

  /**
   * This method reads user data from the users.txt file given a username and password.
   *
   * @param username The user's username.
   * @param password The user's password.
   * @return The user's info if the username and password are correct, "!noaccount!" otherwise.
   * @throws RemoteException If there is a communication-related exception that may occur during the
   *                         execution of a remote method call.
   */
  @Override
  public synchronized String readFromFile(String username, String password) throws RemoteException {
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

  /**
   * This method updates the score of a user in the users.txt file.
   *
   * @param username The user's username.
   * @throws IOException If there is an error writing to the file.
   */
  @Override
  public void updateScore(String username) throws IOException {
    StringBuilder sb = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] fields = line.split(" ");

        if (fields[0].equals(username)) {
          fields[2] = Integer.toString(Integer.parseInt(fields[2]) + SCORE_INCREMENT);
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

  @Override
  public boolean isAlive() throws RemoteException {
    return true;
  }
}
