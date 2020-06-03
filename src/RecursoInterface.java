import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RecursoInterface extends Remote {

    public int ler() throws RemoteException;

    public int escrever(int numero) throws RemoteException;

}
