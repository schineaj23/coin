import java.util.ArrayList;

/*  Resources for this:
    https://en.wikipedia.org/wiki/Merkle_tree
    https://medium.com/@vinayprabhu19/merkel-tree-in-java-b45093c8c6bd */ 
public class MerkleTree {
    public byte[] calculateMerkleTreeRoot(Transaction[] transactions) {
        ArrayList<byte[]> transactionHashes = new ArrayList<>();
        for(Transaction t : transactions) {
            transactionHashes.add(t.hash());
        }
        ArrayList<byte[]> merkleRootHashes = merkleTree(transactionHashes);
        return merkleRootHashes.get(0); // Return the root
    }

    private ArrayList<byte[]> merkleTree(ArrayList<byte[]> hashes) {
        // Return the root if we only have one left
        if(hashes.size() == 1)
            return hashes;
        
        ArrayList<byte[]> parentHashList = new ArrayList<>();
        // Hash the leaf transaction pair to get parent transaction
        for(int i=0;i<hashes.size();i+=2) {
            byte[] hashCombination = Util.concatenateBuffers(hashes.get(i), hashes.get(i+1));
            parentHashList.add(hashCombination);
        }
        // If odd number of transactions, add last transaction again!
        if(hashes.size() % 2 == 1) {
            byte[] lastHash = hashes.get(hashes.size()-1);
            byte[] buffer = Util.concatenateBuffers(lastHash, lastHash);
            parentHashList.add(buffer);
        }
        return merkleTree(parentHashList);
    }
}
