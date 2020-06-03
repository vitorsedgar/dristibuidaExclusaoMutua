import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface ServidorInterface extends Remote {

    String registrarCliente(ClienteInterface clienteInterface) throws RemoteException;

    String registrarRecurso(RecursoInterface recursoInterface) throws RemoteException;

    RecursoInterface solicitarRecurso() throws RemoteException;

    void liberar() throws RemoteException;

    void bloquear() throws RemoteException;

}
