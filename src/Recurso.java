import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Recurso extends UnicastRemoteObject implements RecursoInterface {

    private static final long serialVersionUID = 1L;

    private static final String ARQUIVO = "recurso.txt";

    private static ServidorInterface servidor;

    protected Recurso() throws RemoteException {
    }

    public static void iniciar(InetAddress hostAddr) throws IOException {
        Recurso recurso = new Recurso();

        try {
            //Registra no RMI Registry o objeto
            Naming.rebind("Recurso", recurso);
        } catch (Exception e) {
            System.out.println("Recurso failed: " + e);
        }

        String remoteHostName = hostAddr.getHostAddress();
        String connectLocation = "//" + remoteHostName + "/Servidor";

        servidor = null;
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
            //Registra recurso no servidor
            ip = servidor.registrarRecurso(recurso);
            System.out.println("Call to Servidor...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int ler() throws RemoteException {
        //Recebe um pedido pelo recurso
        System.out.println("Enviando dado");
        try {
            //Le arquivo
            FileReader fileReader = new FileReader(ARQUIVO);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String dado = "";
            String linhaAtual;

            while ((linhaAtual = bufferedReader.readLine()) != null)
            {
                dado = linhaAtual;
            }

            fileReader.close();

            //Chama o objeto remoto do peer que solicitou o dado e o envia
            return Integer.parseInt(dado);

        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void escrever(int numero) throws IOException {
        System.out.println("Escrevendo dado");
        String data = "\n" + numero;
        Files.write(Paths.get(ARQUIVO), data.getBytes(), StandardOpenOption.APPEND);
    }
}
