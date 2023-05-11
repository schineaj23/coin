package asch.coin;

// This contains all verification mechanisms for blocks
public class Proof {
    public static final int NUM_ZEROS = 2;

    // Checks if a block is valid (proven accurate) by "work"
    // This "work" is the amount
    public static boolean isBlockValid(Block block) {
        byte[] blockHash = block.hash();
        for (int i = 0; i < NUM_ZEROS; i++) {
            if (blockHash[i] != 0) {
                // Prints take up a lot of time. This should not run other than debugging!
                // System.out.printf("Block INVALID: %s Nonce: %d\n",
                // Util.bytesToHex(blockHash), block.nonce);
                return false;
            }
        }
        // System.out.printf("Block Valid: %s\nNonce: %d\n", Util.bytesToHex(blockHash), block.nonce);
        return true;
    }

    // Iterates nonce until the block is valid.
    // WARNING: This method mutates the actual block's nonce and returns the result
    public static int hashUntilValid(Block block) {
        System.out.printf("Proof::hashUntilValid() started for block: %s\n", Util.bytesToHex(block.hash()));
        long startTime = System.currentTimeMillis();
        while (!isBlockValid(block)) {
            block.nonce++;
        }
        double timeElapsedSeconds = (System.currentTimeMillis() - startTime) / 1000f;
        System.out.printf("Proof::hashUntilValid(): Nonce Found! Took %.2f seconds to find nonce.\n",
                timeElapsedSeconds);
        return block.nonce;
    }
}
