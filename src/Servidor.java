import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class Servidor extends UnicastRemoteObject implements ServidorInterface {

    private static final long serialVersionUID = 1L;

    private static volatile RecursoInterface recurso;
    private static volatile ArrayList<ClienteInterface> clientes;

    public static boolean RECURSO_LOCKADO = false;

    protected Servidor() throws RemoteException {
    }

    public static void iniciar() throws IOException {
        clientes = new ArrayList<>();

        try {
            Naming.rebind("Servidor", new Servidor());
            System.out.println("Servidor is ready.");
        } catch (Exception e) {
            System.out.println("Servidor failed: " + e);
        }
    }

    @Override
    public String registrarCliente(ClienteInterface cliente) throws RemoteException {

        try {
            String IPAdress = getClientHost();

            System.out.println("Registrando cliente " + IPAdress);

            //Registra o Cliente
            clientes.add(cliente);
            return IPAdress;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String registrarRecurso(RecursoInterface recursoInterface) throws RemoteException {

        try {
            String IPAdress = getClientHost();

            System.out.println("Registrando recurso " + IPAdress);

            recurso = recursoInterface;
            return IPAdress;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public RecursoInterface solicitarRecurso() throws RemoteException {
        System.out.println("Recurso Solicitado");
        //Ao solicitar um recurso especifico o programa Retorna um objeto remoto de onde este se encontra
        if(RECURSO_LOCKADO) {
            return null;
        }
        bloquear();
        return null;
    }

    @Override
    public void bloquear() {
        RECURSO_LOCKADO = true;
    }

    public void liberar() {
        RECURSO_LOCKADO = false;
    }

}
