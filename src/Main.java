import java.security.*;
public class Main {
     public static void main(String[] args) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = keyGen.generateKeyPair();


        CoinTransaction t = new CoinTransaction();
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(pair.getPrivate());

            sig.update(t.toString().getBytes());
            byte[] signature = sig.sign();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}