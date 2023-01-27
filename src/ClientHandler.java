import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

  private Socket clientSocket;

  ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    System.out.println("Connected, listening to client at: " + clientSocket);
    try {
      InputStreamReader socketInputReader = new InputStreamReader(clientSocket.getInputStream());
      OutputStream socketOutStream = clientSocket.getOutputStream();

      BufferedReader socketInput = new BufferedReader(socketInputReader);
      PrintWriter socketOutput = new PrintWriter(socketOutStream, true);

      // String to read message from client
      String clientResponse;

      // While loop to keep reading until "EXIT" is input
      while (true) {
        try {
          // Initial prompt and read response
          socketOutput.println("Please enter a command (PLAY or EXIT)");
          clientResponse = socketInput.readLine();

          // Break from loop if "EXIT" is input
          if (clientResponse.equalsIgnoreCase("EXIT")) {
            System.out.println("Client: " + clientSocket + " exited.");
            break;
          }
          // Search the database for the item matching the ID
          else if (clientResponse.equalsIgnoreCase("PLAY")) {
            System.out.println("The client wishes to " + clientResponse);
            socketOutput.println("Please enter a word: ");
            String guess = socketInput.readLine();

            String response = "You guessed: " + guess;

            // Echo the user input back to them
            socketOutput.println(response);
          }
          // If the user does not input "PLAY" or "EXIT"
          else {
            System.out.println("Client has chosen an invalid command.");
            socketOutput.println("Invalid command, please try again.");
          }

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      // Close connection
      socketInput.close();
      socketOutput.close();
      clientSocket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
