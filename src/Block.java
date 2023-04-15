import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Block {
    // For this implementation, only 10 transactions per block
    public static final int TRANSACTION_COUNT = 3;
    
    public CoinTransaction[] transactions = new CoinTransaction[TRANSACTION_COUNT];
    public byte[] timestampHash;

    public byte[] hash() throws NoSuchAlgorithmException {
        // The hash of all the transactions (each follows the next)
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Making the assumption that when I hash this it is for the next block
        // Or else there may be null transactions, I don't feel like dealing with this :D
        for(int i=0;i<TRANSACTION_COUNT;i++) {
            // Adding the hash of each transaction to the total block digest
            md.update(transactions[i].hash(), i * 256 / 8, 256 / 8);
        }

        // FIXME: the timestamp should retrieve from our decentralized timestamp server
        // Since that does not exist yet just use the system time for now :D
        md.update(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array(), TRANSACTION_COUNT * 256 * 8, 8);

        // If everything went correctly, we should only have our 10 transactions and the hash of unix time
        assert md.getDigestLength() == (TRANSACTION_COUNT * 256 / 8) + 8;

        return md.digest();
    }

    public boolean pushTransaction(CoinTransaction transaction) {
        if (transaction == null)
            return false;
        for (int i = 0; i < TRANSACTION_COUNT; i++) {
            if (transactions[i] == null) {
                transactions[i] = transaction;
                return true;
            }
        }
        return false;
    }
}