import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class AccountServer {
    //Bound to port 4777
    public static void main(String[] args){
        try
        {
            AccountInterface service = new AccountService();
            LocateRegistry.createRegistry(4777);
            Naming.bind("rmi://localhost:4777", service);

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
