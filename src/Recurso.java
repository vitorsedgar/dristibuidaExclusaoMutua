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

public class Recurso extends UnicastRemoteObject implements RecursoInterface {

    private static final long serialVersionUID = 1L;

    private static final String

    protected Recurso() throws RemoteException {
    }

    @Override
    public int ler() throws RemoteException {
        return 0;
    }

    @Override
    public int escrever(int numero) throws RemoteException {
        return 0;
    }
}
