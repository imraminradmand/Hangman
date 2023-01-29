import netscape.javascript.JSObject;

import java.io.*;
import java.net.InetAddress;
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

        Socket socket = new Socket(InetAddress.getLocalHost(), 7777);
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);

      // While loop to keep reading until "EXIT" is input
      while (true) {
        try {
          // Initial prompt and read response
          clientResponse = socketInput.readLine();

          String[] args = clientResponse.split(" ");

          if (args[0].equalsIgnoreCase("$")){

              socketOut.println("get " + args[1] + " " + args[2]);

              String result = socketIn.readLine();
              socketOutput.println("High-score for " + result.split(" ")[0] + " is " + result.split(" ")[2]);

          }else if (args[0].equalsIgnoreCase("!")){
            socketOut.println("post " + args[1] + " " + args[2] + " " + args[3]);
          }

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      // Close connection
     // socketInput.close();
     // socketOutput.close();
     // clientSocket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
