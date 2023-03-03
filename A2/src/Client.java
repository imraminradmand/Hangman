import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    private static String username;
    private static String password;

    public static void main(String[] args) throws IOException, NotBoundException {
        GameHandlerService service = (GameHandlerService) Naming.lookup("rmi://localhost:4777" + "/GameServer");

        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader stdin = new BufferedReader(inputStreamReader);


        while(true){
            String input = stdin.readLine();
            String[] clientArgs = input.split(" ");

            if(input.equalsIgnoreCase("exit")){
                break;
            }

            if (clientArgs[0].equalsIgnoreCase("login")){
                if(clientArgs.length == 3){
                    if (service.login(clientArgs[1], clientArgs[2])){
                        username = clientArgs[1];
                        password = clientArgs[2];
                        playGame(service);
                        break;
                    }
                }
            }else if(clientArgs[0].equalsIgnoreCase("register")){
                if(clientArgs.length == 3){
                    if(service.register(clientArgs[1], clientArgs[2])) {
                        username = clientArgs[1];
                        password = clientArgs[2];
                        playGame(service);
                        break;
                    }
                }
            }
        }
        inputStreamReader.close();
        stdin.close();
    }

    private static void playGame(GameHandlerService service) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader stdin = new BufferedReader(inputStreamReader);
        String output = "Start a game with start <number of words> <attempts>";

        while(true){
            System.out.println(output);
            String[] args = stdin.readLine().split(" ");

            if(args[0].equalsIgnoreCase("exit")){
                break;
            }


        }
    }
}
