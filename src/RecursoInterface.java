import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RecursoInterface extends Remote {

    int ler(ClienteInterface cliente) throws RemoteException;

    void escrever(int numero) throws RemoteException;

}
