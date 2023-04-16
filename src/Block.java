import java.nio.ByteBuffer;
import java.util.HashSet;

public class Block {
    public HashSet<Transaction> transactions = new HashSet<>();
    public byte[] timestampHash;
    
    public byte[] merkleRoot;

    // The previous block hash links the blocks together!
    public byte[] previousBlockHash;

    // FIXME: implement mining!
    // Data added to merkle root to calculate a hash with the desired # of zeros so it's accepted by others 
    public int nonce;

    public byte[] hash() {
        MerkleTree merkleTree = new MerkleTree();
        byte[] root = merkleTree.calculateMerkleTreeRoot(transactions.toArray(new Transaction[transactions.size()]));
        assert root != null;
        
        byte[] hash = Util.concatenateBuffers(root, ByteBuffer.allocate(4).putInt(nonce).array());
        assert hash != null;
        
        return hash;
    }
}