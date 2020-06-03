import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class Servidor extends UnicastRemoteObject implements ServidorInterface {

    private static final long serialVersionUID = 1L;

    private static volatile ArrayList<RegistroRecurso> recursos;
    private static volatile HashMap<String, RegistroCliente> clientes;


    protected Servidor() throws RemoteException {
    }

    public static void iniciar() throws IOException {
        recursos = new ArrayList<>();
        clientes = new HashMap<>();


        try {
            Naming.rebind("Servidor", new Servidor());
            System.out.println("Servidor is ready.");
        } catch (Exception e) {
            System.out.println("Servidor failed: " + e);
        }
        while (true) {
            try {
                ArrayList<String> eliminados = new ArrayList<>();
                //Verifica se algum cliente não mandou uma msg para o servidor por mais de 10 segundos
                clientes.entrySet().stream().forEach(entry -> {
                    if (System.currentTimeMillis() - entry.getValue().getUltimaInteracao() > 10000) {
                        eliminados.add(entry.getKey());
                        System.out.println("Cliente " + entry.getKey() + " encerrado");
                        try{
                            clientes.get(entry.getKey()).getCliente().remover();
                        } catch (RemoteException e) {
                            System.out.println("Cliente já desconectado");
                        }
                    }
                });
                //Caso algum cliente não tenha enviado, seus recursos são removidos do servidor
                if (!eliminados.isEmpty()) {
                    List<RegistroRecurso> recursosEliminados = recursos.stream()
                            .filter(recurso -> eliminados.contains(recurso.getIp()))
                            .collect(Collectors.toList());
                    eliminados.forEach(eliminado -> clientes.remove(eliminado));
                    recursosEliminados.forEach(eliminado ->
                            recursos.removeIf(recurso ->
                                    recurso.getIp().equalsIgnoreCase(eliminado.getIp())
                            )
                    );
                }
            } catch (ConcurrentModificationException e) {
                System.out.println("Erro ao eliminar: " + e);
            }
        }
    }

    @Override
    public String registrar(HashMap<String, String> arquivos,
                            ClienteInterface clienteInterface) throws RemoteException {


        try {
            String IPAdress = getClientHost();

            System.out.println("Registrando recursos de " + IPAdress);


            //Para cada arquivo do cliente é gravado um registro de recurso no servidor
            arquivos.forEach((key, value) -> {
                RegistroRecurso registroRecurso = new RegistroRecurso();
                registroRecurso.setIp(IPAdress);
                registroRecurso.setHash(value);
                registroRecurso.setNome(key);
                recursos.add(registroRecurso);
            });
            //Registra o Cliente em um HashMap<IP, Objeto de Registro do cliente>
            RegistroCliente cliente = new RegistroCliente();
            cliente.setIp(IPAdress);
            cliente.setUltimaInteracao(System.currentTimeMillis());
            cliente.setCliente(clienteInterface);
            clientes.put(IPAdress, cliente);
            return IPAdress;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public int ping(String ip) throws RemoteException {
        RegistroCliente cliente = clientes.get(ip);
        cliente.setUltimaInteracao(System.currentTimeMillis());
        return 0;
    }

    @Override
    public List<String> solicitar() throws RemoteException {
        System.out.println("Recursos Solicitados");
        //Retorna uma lista de todos recursos
        return recursos.stream()
                .map(recurso -> recurso.getNome() + " - " + recurso.getHash() + " - " + recurso.getIp())
                .collect(Collectors.toList());

    }

    @Override
    public ClienteInterface solicitarRecurso(String nomeArquivo) throws RemoteException {
        System.out.println("Recurso Solicitado " + nomeArquivo);
        //Ao solicitar um recurso especifico o programa Retorna um objeto remoto de onde este se encontra
        Optional<String> ip = recursos.stream()
                .filter(recurso -> recurso.getNome().equalsIgnoreCase(nomeArquivo))
                .map(RegistroRecurso::getIp)
                .findFirst();
        if (ip.isPresent()) {
            return clientes.get(ip.get()).getCliente();
        }
        return null;
    }
}
