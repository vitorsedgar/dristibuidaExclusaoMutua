import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClienteInterface extends Remote {

    public int remover() throws RemoteException;

    public int solicitarRecurso(String nome, ClienteInterface cliente) throws RemoteException;

    public int receberArquivo(String nome, byte[] recurso) throws RemoteException;

    public int receberDado(int dado) throws RemoteException;

}
