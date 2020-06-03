import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface ServidorInterface extends Remote {

    public String registrar(HashMap<String, String> arquivos, ClienteInterface cliente) throws RemoteException;

    public int ping(String IPAdress) throws RemoteException;

    public List<String> solicitar() throws RemoteException;

    public ClienteInterface solicitarRecurso(String nomeArquivo) throws RemoteException;
}
