package TCP;

import Utils.EncryptionLogic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    EncryptionLogic logic = new EncryptionLogic();
    private Scanner sc = new Scanner(System.in);
    static final int PORT = 9090;
    private boolean end = false;

    public void listen(){
        ServerSocket serverSocket=null;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            while(!end){
                clientSocket = serverSocket.accept();
                System.out.println("Conexió amb: " + clientSocket.getInetAddress());
                //processem la petició del client
                proccesClientRequest(clientSocket);
                //tanquem el sòcol temporal per atendre el client
                closeClient(clientSocket);
            }
            //tanquem el sòcol principal
            if(serverSocket!=null && !serverSocket.isClosed()){
                serverSocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void proccesClientRequest(Socket clientSocket) throws IOException {
        boolean farewellMessage=false;
        String clientMessage="";

        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

        try {
            System.out.println("Canals de e/s establecidos...");

            //Generar el par de claves
            KeyPair keys = logic.generarClave();

            //Extraer las claves
            PublicKey publicKey = keys.getPublic();
            PrivateKey privateKey = keys.getPrivate();

            System.out.println("Enviament de claus en procés...");
            //Recibe clave pública del cliente
            PublicKey publicKeyClient = (PublicKey) ois.readObject();

            //Envia clave pública del servidor
            oos.writeObject(publicKey);
            oos.flush();

            System.out.println("Procés acabat. Esperant que el client comenci la conversa.");

            do{

                //Recibir mensaje y desencriptar
                byte[] msgClient = (byte[]) ois.readObject();
                clientMessage = decryptMessage(msgClient, privateKey);

                farewellMessage = isFarewellMessage(clientMessage);

                //Enviar mensaje y encriptar
                oos.writeObject(encryptMessage(publicKeyClient));
                oos.flush();

            }while((clientMessage)!=null && !farewellMessage);

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptMessage(byte[] msgClient, PrivateKey privateKey) {
        String msg = new String(logic.desencriptar(msgClient, privateKey));
        System.out.println("TCP.Client: " + msg);

        return msg;
    }

    public byte[] encryptMessage(PublicKey publicKeyClient) {
        System.out.print("TCP.Server: ");
        String msg = sc.nextLine();

        return logic.encriptar(msg.getBytes(), publicKeyClient);
    }

    private boolean isFarewellMessage(String msg) {
        if(msg.equals("bye")) return true;
        else return false;
    }

    private void closeClient(Socket clientSocket){
        //si falla el tancament no podem fer gaire cosa, només enregistrar
        //el problema
        try {
            //tancament de tots els recursos
            if(clientSocket!=null && !clientSocket.isClosed()){
                if(!clientSocket.isInputShutdown()){
                    clientSocket.shutdownInput();
                }
                if(!clientSocket.isOutputShutdown()){
                    clientSocket.shutdownOutput();
                }
                clientSocket.close();
            }
        } catch (IOException ex) {
            //enregistrem l'error amb un objecte Logger
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Server tcpSocketServer = new Server();
        tcpSocketServer.listen();
    }
}