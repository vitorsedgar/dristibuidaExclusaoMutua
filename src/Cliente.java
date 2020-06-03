import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

public class Cliente extends UnicastRemoteObject implements ClienteInterface {

    private static final long serialVersionUID = 1L;

    private static volatile Boolean ligado;

    protected Cliente() throws RemoteException {
    }

    public static void iniciar(InetAddress hostAddr) throws IOException {
        ligado = Boolean.TRUE;
        Scanner scanner = new Scanner(System.in);
        Cliente cliente = new Cliente();

        try {
            //Registra no RMI Registry o objeto
            Naming.rebind("Cliente", cliente);
        } catch (Exception e) {
            System.out.println("Cliente failed: " + e);
        }

        String remoteHostName = hostAddr.getHostAddress();
        String connectLocation = "//" + remoteHostName + "/Servidor";

        ServidorInterface servidor = null;
        try {
            //Conecta no host e busca seu objeto remoto no Registro RMI do Servidor
            System.out.println("Conectando ao Servidor em : " + connectLocation);
            servidor = (ServidorInterface) Naming.lookup(connectLocation);
        } catch (Exception e) {
            System.out.println("Servidor falhou: ");
            e.printStackTrace();
        }

        String ip = null;

        try {
            //Registra cliente no servidor, com seus arquivos disponiveis
            ip = servidor.registrarCliente(cliente);
            System.out.println("Call to Servidor...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        while (ligado) {
            System.out.print("Realizar ação (A) ou Sair (Q): ");
            String acao = scanner.next();
            if (!ligado) break;
            switch (acao) {
                case "A":
                    try {
                        RecursoInterface peer = null;
                        while (Objects.isNull(peer)) {
                            //Recebe um objeto remoto do servidor para interação com recurso
                            System.out.println("Solicitando recurso");
                            peer = servidor.solicitarRecurso();
                            if (Objects.nonNull(peer)) {
                                for(int i=0; i<50; i++) {
                                    int dado = peer.ler() + 1;
                                    peer.escrever(dado);
                                }
                            } else {
                                System.out.println("Recurso indisponivel");
                                Thread.sleep(1000);
                            }
                        }
                    } catch (RemoteException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case "Q":
                    ligado = Boolean.FALSE;
                    break;
                default:
                    System.out.print("'" + acao + "' não é uma ação possivel");
                    break;
            }
        }
        System.exit(1);
    }
}
