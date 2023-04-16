import java.nio.ByteBuffer;
import java.security.*;

// This part is following the Bitcoin spec from the bitcoin white paper (Transactions section)
// For the moment is mainly just a struct that contains the data for a transaction
public class Transaction {
    public PublicKey newOwnerKey;
    public byte[] oldOwnerSignature;
    public byte[] timestampHash;
    public double amount;

    public byte[] hash() {
        System.out.println("Transaction::hash() called");
        int keyLen = newOwnerKey.getEncoded().length;
        byte[] key = new byte[keyLen + timestampHash.length + oldOwnerSignature.length + 8];

        // Copy all the data into one array to be hashed
        System.arraycopy(newOwnerKey.getEncoded(), 0, key, 0, keyLen);
        System.out.println("Copied newOwnerKey");

        System.arraycopy(timestampHash, 0, key, keyLen, timestampHash.length);
        System.out.println("Copied timeStampHash");

        System.arraycopy(oldOwnerSignature, 0, key, keyLen + timestampHash.length, oldOwnerSignature.length);
        System.out.println("Copied oldOwnerSignature");

        byte[] amountBytes = ByteBuffer.allocate(8).putDouble(amount).array();
        System.arraycopy(amountBytes, 0, key, keyLen + timestampHash.length + oldOwnerSignature.length, 8);
        System.out.println("Copied amount (bytes representation)");

        // Add this all to one array and hash it
        System.out.println("Hashing transaction");
        byte[] hashed = Util.hashBuffer(key);

        // If this is null, something went horribly wrong!
        assert hashed != null;

        System.out.printf("Transaction::hash() result: %s\n", Util.bytesToHex(hashed));
        return hashed;
    }
}
