import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;

public interface AccountInterface extends Remote {
    public boolean writeToFile(String[] args) throws IOException;
    public String readFromFile(String[] args);
}
