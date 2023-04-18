import java.util.ArrayList;
import java.util.Set;

/*  Resources for this:
    https://en.wikipedia.org/wiki/Merkle_tree */
public class MerkleTree {
    public byte[] calculateMerkleTreeRoot(Set<byte[]> hashSet) {
        ArrayList<byte[]> merkleRootHashes = merkleTree(new ArrayList<>(hashSet));
        return merkleRootHashes.get(0); // Return the root
    }

    // Potentially refactor this to an iterative solution if the performance really stinks
    private ArrayList<byte[]> merkleTree(ArrayList<byte[]> hashes) {
        // Return the root if we only have one left
        if(hashes.size() == 1)
            return hashes;

//        System.out.printf("hashes.size(): %d\n", hashes.size());
        ArrayList<byte[]> parentHashList = new ArrayList<>();
        // Hash the leaf transaction pair to get parent transaction
        for(int i=0;i<hashes.size();i+=2) {
            int secondEntry = Math.min(i+1, hashes.size()-1);
//            System.out.printf("%d,%d\n", i, secondEntry);
            byte[] hashCombination = Util.concatenateBuffers(hashes.get(i), hashes.get(secondEntry));
            parentHashList.add(hashCombination);
        }
        return merkleTree(parentHashList);
    }
}
