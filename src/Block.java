import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Block extends Hashable {
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    public byte[] timestampHash;
    public byte[] merkleRoot;

    // The previous block hash links the blocks together!
    public byte[] previousBlockHash;

    // FIXME: implement mining!
    // Data added to merkle root to calculate a hash with the desired # of zeros so it's accepted by others 
    public int nonce;

    // Why are we creating a block if we don't have an initial transaction?
    public Block(Transaction initialTransaction) {
        assert initialTransaction != null;
        transactions.add(initialTransaction);
    }

    public boolean addTransaction(Transaction t) {
        transactions.add(t);
        return true;
    }

    public boolean contains(TransactionId a) {
        for(Transaction b : transactions) {
            if(a.compareTo(b.getTransactionId()) == 0)
                return true;
        }
        return false;
    }

    public Transaction getRootTransaction() {
        if(transactions.size() < 1) {
            System.out.println("Block::getRootTransaction(): transactions empty!");
            return null;
        }
        return transactions.get(0);
    }

    // FIXME: timestamp bug
    public void timestamp() {
        timestampHash = Util.hashBuffer(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
    }

    public byte[] hash() {
        // Before hashing the entire object, calculate our merkle root
        MerkleTree merkleTree = new MerkleTree();
        byte[] root = merkleTree.calculateMerkleTreeRoot(transactions);
        merkleRoot = root;

        // Ok, now hash the entire object + the nonce
        byte[] hashedObject = super.hash();
        assert hashedObject != null;
        return Util.hashBuffer(Util.concatenateBuffers(hashedObject, ByteBuffer.allocate(4).putInt(nonce).array()));
    }

    @Override
    public String toString() {
        String trans = "\n";
        for(Transaction t : transactions) {
            trans += t;
        }

        return String.format("""
                [BLOCK INFO]
                Block Hash: %s
                Previous Block Hash: %s
                Timestamp Hash: %s
                Nonce: %d
                Transaction Count: %d
                Transactions: %s
                """, Util.bytesToHex(hash()), Util.bytesToHex(previousBlockHash), Util.bytesToHex(timestampHash), nonce, transactions.size(), trans);
    }
}