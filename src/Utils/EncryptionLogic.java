package Utils;

import javax.crypto.Cipher;
import java.security.*;

public class EncryptionLogic {
    String algoritmo;

    public EncryptionLogic() {
        algoritmo = "RSA";
    }

    public KeyPair generarClave() {
        KeyPair keys = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algoritmo);
            keyGen.initialize(1024);
            keys = keyGen.genKeyPair();
        } catch (Exception e) {
            System.out.println("Generados no disponible");
        }
        return keys;
    }

    public byte[] encriptar(byte[] inputBytes, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(algoritmo);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return cipher.doFinal(inputBytes);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public byte[] desencriptar(byte[] inputBytes, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(algoritmo);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(inputBytes);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

}
