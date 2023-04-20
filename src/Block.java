import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Optional;

public class Block {
    private final LinkedHashMap<byte[], Transaction> transactions = new LinkedHashMap<>();
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
        transactions.put(initialTransaction.hash(), initialTransaction);
    }

    public boolean addTransaction(Transaction t) {
        if (transactions.containsKey(t.hash())) return false;
        transactions.put(t.hash(), t);
        return true;
    }

    public boolean contains(Transaction t) {
        return transactions.containsKey(t.hash());
    }

    public boolean contains(byte[] transactionHash) {
        return transactions.containsKey(transactionHash);
    }

    public Optional<Transaction> getRootTransaction() {
        return transactions.values().stream().findFirst();
    }

    // FIXME: timestamp bug
    public void timestamp() {
        timestampHash = Util.hashBuffer(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
    }

    public byte[] hash() {
        MerkleTree merkleTree = new MerkleTree();
        byte[] root = merkleTree.calculateMerkleTreeRoot(transactions.keySet());
        merkleRoot = root;
        assert root != null;
        return Util.hashBuffer(Util.concatenateBuffers(root, ByteBuffer.allocate(4).putInt(nonce).array()));
    }

    @Override
    public String toString() {
        return String.format("""
                [BLOCK INFO]
                Block Hash: %s
                Previous Block Hash: %s
                Transaction Count: %d
                Timestamp Hash: %s
                Nonce: %d
                """, Util.bytesToHex(hash()), Util.bytesToHex(previousBlockHash), transactions.size(), Util.bytesToHex(timestampHash), nonce);
    }
}