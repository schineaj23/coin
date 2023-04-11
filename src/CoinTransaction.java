import java.security.*;

// This part is following the Bitcoin spec from the bitcoin white paper (Transactions section)
public class CoinTransaction {
    public PublicKey newOwnerKey;
    public byte[] oldOwnerSignature;
    public byte[] timestampHash;
    public double amount;

    // FIXME: implement me
    public byte[] hash() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA256");
        byte[] key = newOwnerKey.getEncoded();
        return null;
    }

    public void transferOwnership(CoinTransaction previousTransaction, PrivateKey oldOwnerPrivateKey, PublicKey newOwnerKey, byte[] timestampHash) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA256");
        Signature oldOwnerSignature = Signature.getInstance("SHA256withRSA");

    }
}
