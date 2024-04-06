package TCP;

import Utils.EncryptionLogic;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {
    private Scanner sc = new Scanner(System.in);
    EncryptionLogic logic = new EncryptionLogic();

    public void connect(String address, int port) {
        String request;
        boolean continueConnected = true;
        Socket socket;

        try {
            socket = new Socket(InetAddress.getByName(address), port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            //Generar el par de claves
            KeyPair keys = logic.generarClave();

            //Extraer las claves
            PublicKey publicKey = keys.getPublic();
            PrivateKey privateKey = keys.getPrivate();

            System.out.println("Enviament de claus en procés...");
            //Enviar la clave publica
            oos.writeObject(publicKey);
            oos.flush();

            //Recibe clave pública del servidor
            PublicKey publicKeyServer = (PublicKey) ois.readObject();

            // Mensaje de bienvenida al cliente
            System.out.println("Procés acabat.");
            System.out.println("Benvingut al xat. Escriu un missatge per començar la conversa");

            //el client atén el port fins que decideix finalitzar
            while(continueConnected){

                //Enviar mensaje y encriptar
                oos.writeObject(encryptMessage(publicKeyServer));
                oos.flush();

                //Recibir mesaje y desencriptar
                byte[]  msgSever = (byte[]) ois.readObject();
                request = decryptMessage(msgSever, privateKey);

                continueConnected = mustFinish(request);
            }

            close(socket);
        } catch (UnknownHostException ex) {
            System.out.printf("Error de conexión. No existe el host, %s", ex);
        } catch (IOException ex) {
            System.out.printf("Error de conexión indefinido, %s", ex);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean mustFinish(String request) {
        if(request.equals("bye")) return false;
        else return true;
    }

    private byte[] encryptMessage(PublicKey publicKeyServer) {
        System.out.print("TCP.Client: ");
        String msg = sc.nextLine();

        return logic.encriptar(msg.getBytes(), publicKeyServer);
    }

    private String decryptMessage(byte[] msgServer, PrivateKey privateKey) {
        String msg = new String(logic.desencriptar(msgServer, privateKey ));
        System.out.println("Sever: " + msg);

        return msg;
    }


    private void close(Socket socket){
        //si falla el tancament no podem fer gaire cosa, només enregistrar
        //el problema
        try {
            //tancament de tots els recursos
            if(socket!=null && !socket.isClosed()){
                if(!socket.isInputShutdown()){
                    socket.shutdownInput();
                }
                if(!socket.isOutputShutdown()){
                    socket.shutdownOutput();
                }
                socket.close();
            }
        } catch (IOException ex) {
            //enregistrem l'error amb un objecte Logger
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Client tcpSocketClient = new Client();
        tcpSocketClient.connect("localhost", 9090);
    }
}