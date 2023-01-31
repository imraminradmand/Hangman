import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import netscape.javascript.JSObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientHandler implements Runnable {

  private Socket clientSocket;


  ClientHandler(Socket socket) {
    this.clientSocket = socket;

  }

  private void play(BufferedReader socketInput,
      PrintWriter socketOutput,
      BufferedReader accountIn,
      PrintWriter accountOut,
      String[] args) throws IOException {
    while (true) {
      String startRes = socketInput.readLine();
      String[] startArgs = startRes.split(" ");

      // make UDP connection to word repository
      DatagramSocket wordRepository = new DatagramSocket();
      byte[] buf = new byte[256];
      byte[] inputBuf = new byte[256];

      // TODO: Implement actual game logic here
      if (startArgs[0].equalsIgnoreCase("start")) {
        buf = startRes.getBytes();

        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5599);
        wordRepository.send(packet);

        DatagramPacket wordRepoReply = new DatagramPacket(inputBuf, inputBuf.length, address, 5599);
        wordRepository.receive(wordRepoReply);

        String word = new String(wordRepoReply.getData(), 0, wordRepoReply.getLength());
        socketOutput.println(word);
      } else if (startArgs[0].equalsIgnoreCase("?")) {
        buf = startRes.getBytes();

        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5599);
        wordRepository.send(packet);

        DatagramPacket wordRepoReply = new DatagramPacket(inputBuf, inputBuf.length, address, 5599);
        wordRepository.receive(wordRepoReply);

        String word = new String(wordRepoReply.getData(), 0, wordRepoReply.getLength());
        System.out.println(word);
        String exists = "False";
        if (word.equalsIgnoreCase("true")) {
          exists = "True";
        }
        socketOutput.println(exists);
      } else if (startArgs[0].equalsIgnoreCase("$")) {
        System.out.println("here");
        accountOut.println("get " + args[1] + " " + args[2]);

        String[] result = accountIn.readLine().split(" ");
        if(result.length > 1) {
          socketOutput.println("High-score for " + result[0] + " is " + result[2]);
        }
      } else if (startRes.charAt(0) == '#') {
        break;
      }
    }
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

      Socket accountSocket = new Socket(InetAddress.getLocalHost(), 7777);
      BufferedReader accountIn = new BufferedReader(new InputStreamReader(accountSocket.getInputStream()));
      PrintWriter accountOut = new PrintWriter(accountSocket.getOutputStream(), true);

      // While loop to keep reading until "EXIT" is input
      while (true) {
        try {
          socketOutput.println("Instruction: <login/register> <username> <password> OR EXIT");
          // Initial prompt and read response
          clientResponse = socketInput.readLine();
          String[] args = clientResponse.split(" ");

          if (args[0].equalsIgnoreCase("login")) {
            accountOut.println("get " + args[1] + " " + args[2]);

            String accountResponse = accountIn.readLine();
            socketOutput.println(accountResponse);

            // PLAY GAME
            // TODO: add else so that if account doesn't exist they have to register for account
            if (!accountResponse.equals("!noaccount!")) {
              socketOutput.println("Start new game with the following command - start <number of words> <attempts>");
              play(socketInput, socketOutput, accountIn, accountOut, args);
            }

            // TODO: Check for successful registration then start game
          } else if (args[0].equalsIgnoreCase("register")) {
            accountOut.println("post " + args[1] + " " + args[2] + " 0");
            socketOutput.println(accountIn.readLine());
          } else {
            socketOutput.println("Unknown Command, try again");
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
