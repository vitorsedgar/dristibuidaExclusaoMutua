import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface ServidorInterface extends Remote {

    String registrar(HashMap<String, String> arquivos, ClienteInterface cliente) throws RemoteException;

    String registrarRecurso(RecursoInterface recurso) throws RemoteException;

    int ping(String IPAdress) throws RemoteException;

    List<String> solicitar() throws RemoteException;

    ClienteInterface solicitarRecurso(String nomeArquivo) throws RemoteException;
}
