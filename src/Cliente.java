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
import java.util.Scanner;
import java.util.stream.Stream;

public class Cliente extends UnicastRemoteObject implements ClienteInterface {

    private static final long serialVersionUID = 1L;

    private static volatile HashMap<String, String> arquivosDisponiveis;
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

        //Gera arquivos disponiveis no cliente
        HashMap<String, String> arquivosDisponiveis = getArquivosDisponiveis();

        String ip = null;

        try {
            //Registra cliente no servidor, com seus arquivos disponiveis
            ip = servidor.registrar(arquivosDisponiveis, cliente);
            System.out.println("Call to Servidor...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //Inicia uma thread para mandar uma mensagem a cada 5 seg para informar que continua vivo ao servidor
        Thread pingThread = new Thread(new Ping(servidor, ip));
        pingThread.start();


        while (ligado) {
            System.out.print("Solicitar arquivos Disponiveis (S), Solicitar Recurso (SR) ou Sair (Q): ");
            String acao = scanner.next();
            if (!ligado) break;
            switch (acao) {
                case "S":
                    try {
                        List<String> recursos = servidor.solicitar();
                        recursos.forEach(System.out::println);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case "SR":
                    System.out.print("Digitar nome do arquivo: ");
                    String arquivo = scanner.next();
                    try {
                        //Recebe um objeto remoto do servidor, indicando onde o arquivo se encontra
                        ClienteInterface peer = servidor.solicitarRecurso(arquivo);
                        if (peer == null) {
                            System.out.println("Arquivo não existe");
                        } else {
                            try {
                                //Se comunica com o cliente pedindo o arquivo dele
                                System.out.println("Solicitando " + arquivo);
                                peer.solicitarRecurso(arquivo, cliente);
                            } catch (Exception e) {
                                System.out.println("Cliente failed: ");
                                e.printStackTrace();
                            }
                        }
                    } catch (RemoteException e) {
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
        pingThread.interrupt();
        System.exit(1);
    }

    private static HashMap<String, String> getArquivosDisponiveis() {
        HashMap<String, String> arquivosDisponiveis = new HashMap<>();

        try (Stream<Path> walk = Files.walk(Paths.get("disponiveis"))) {

            walk.filter(Files::isRegularFile).forEach(arquivo -> {
                        try {
                            arquivosDisponiveis.put(arquivo.getFileName().toString(), geraHash(arquivo.toString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            );

        } catch (IOException e) {
            e.printStackTrace();
        }

        return arquivosDisponiveis;

    }

    private static String geraHash(String arquivo) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(arquivo), md)) {
            while (dis.read() != -1) ;
            md = dis.getMessageDigest();
        }

        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }

        return result.toString();

    }

    @Override
    public int remover() throws RemoteException {
        System.out.println("Peer removido pelo host");
        ligado = Boolean.FALSE;
        return 0;
    }

    @Override
    public int solicitarRecurso(String nome, ClienteInterface cliente) throws RemoteException {
        //Recebe um pedido por um recurso que se tem disponivel
        System.out.println("Enviando arquivo");
        try {
            //Le o arquivo e o transforma em um vetor de bytes
            File file = new File("disponiveis/" + nome);
            FileInputStream fis = new FileInputStream(file);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            try {
                for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                    bos.write(buf, 0, readNum);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] bytes = bos.toByteArray();
            //Chama o objeto remoto do peer que solicitou o arquivo e o envia o vetor de bytes
            cliente.receberArquivo(nome, bytes);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int receberArquivo(String nome, byte[] recurso) throws RemoteException {
        System.out.println("Recebendo arquivo " + nome + " - " + recurso.length);
        //Recebe o vetor de bytes do outro peer e então o transforma em um arquivo na pasta 'disponiveis/'
        File someFile = new File("disponiveis/" + nome);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(someFile);
            fos.write(recurso);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
