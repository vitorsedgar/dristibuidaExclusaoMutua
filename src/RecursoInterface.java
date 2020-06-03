import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RecursoInterface extends Remote {

    int ler() throws RemoteException;

    void escrever(int numero) throws IOException;

}
