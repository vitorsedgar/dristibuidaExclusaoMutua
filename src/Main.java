import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Main {

    public static void main(String[] args) throws IOException {
        
        /**
            Integrantes: Vitor Edgar, Gabriel Löff e Matheus Ferreira
            Estratégia Utilizada: Algoritimo Centralizado
            Tecnologia Utilizada: RMI
        **/

        if (args.length < 3) {
            System.out.println("Usage: java Main <cliente/servidor/recurso> <ip-local> <ip-servidor>");
            System.exit(1);
        }

        InetAddress hostAdress = InetAddress.getByName(args[2]);


        try {
            //Deve se cuidar para definir o hostlocal, senão acaba ocorrendo erro de conexão ao tentar interagir com o arquivo
            System.setProperty("java.rmi.server.hostname", args[1]);
            LocateRegistry.createRegistry(1099);
            System.out.println("java RMI registry created.");
        } catch (RemoteException e) {
            System.out.println("java RMI registry already exists.");
        }

        if (args[0].equalsIgnoreCase("cliente")) {
            Cliente.iniciar(hostAdress);
        } else if (args[0].equalsIgnoreCase("recurso")) {
            Recurso.iniciar(hostAdress);
        } else {
            Servidor.iniciar();
        }
    }

}
