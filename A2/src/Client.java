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
        GameHandlerInterface service = (GameHandlerInterface) Naming.lookup("rmi://localhost:4777" + "/GameServer");

        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader stdin = new BufferedReader(inputStreamReader);


        while(true){
            System.out.println("USAGE: login <username> <password> OR register <username> <password> OR exit");
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

    private static void playGame(GameHandlerInterface service) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader stdin = new BufferedReader(inputStreamReader);
        System.out.println("Start a game with start <number of words> <attempts>");

        while(true){
            String[] args = stdin.readLine().split(" ");

            if(args[0].equalsIgnoreCase("exit")){
                service.logOut(username);
                break;
            }

            if(args.length == 1){
                /*
                * Check game commands that are only one character
                * */
                
                if(args[0].equals("!")){
                    System.out.println(service.restartGame(username));
                }else if(args[0].equals("#")){
                    System.out.println(service.endGame(username));
                }else if(args[0].equals("!help")){
                    writeHelpScreen();
                }else if (args[0].equals("$")){
                    System.out.println(service.getScore(username, password));
                }else{
                    if(args[0].length() > 1){
                        System.out.println(service.guessPhrase(username, args[0]));
                    }else{
                        System.out.println(service.guessLetter(username, args[0].charAt(0)));
                    }
                }
            }else if (args.length == 2){
                /*
                * Write commands here that require two arguments such as ?word <word>
                * */
                if (args[0].equals("?")) {
                    String word = args[1];
                    System.out.println(word + ": " + service.checkWord(word));
                } else if (args[0].equals("+")) {
                    String word = args[1];
                    boolean response = service.addWord(word);
                    System.out.println(word + ": " + (response ? "added" : "exists"));
                } else if (args[0].equals("-")) {
                    String word = args[1];
                    boolean response = service.removeWord(word);
                    System.out.println(word + ": " + (response ? "removed" : "does not exist"));
                }
            }else if (args.length == 3){
                if(args[0].equalsIgnoreCase("start")){
                    System.out.println(service.startGame(username, Integer.parseInt(args[1]), Integer.parseInt(args[2])));
                }
            }else{
                System.out.println("Unknown commands, use !help for a list of options.");
            }
        }
    }

    private static void writeHelpScreen(){
        System.out.println("-----Commands-----");
        System.out.println("start <number of letters> <number of words>");
        System.out.println("# - Ends the game");
        System.out.println("?word <word>- Checks if a word exists in the repository");
        System.out.println("$ - Retrieves a users score");

    }
}
