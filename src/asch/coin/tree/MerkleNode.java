package asch.coin.tree;
import java.nio.ByteBuffer;

public class MerkleNode {
    private byte[] hash;
    private MerkleNode left;
    private MerkleNode right;

    public MerkleNode(MerkleNode left, MerkleNode right, byte[] hash) {
        this.left = left;
        this.right = right;
        this.hash = hash;
    }

    public MerkleNode getRight() {
        return right;
    }

    public void setRight(MerkleNode right) {
        this.right = right;
    }

    public MerkleNode getLeft() {
        return left;
    }

    public void setLeft(MerkleNode left) {
        this.left = left;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public byte[] serialize() {
        System.out.println("Node hash length: " + hash.length);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 4 + 32);
        // This is a leaf
        if(isLeaf()) {
            byte[] LEAF_HEAD = {(byte)0xFE, (byte)0xFE};
            byteBuffer.put(LEAF_HEAD);
        } else {
            byte[] NODE_HEAD = {(byte)0xBE, (byte)0xAD};
            byteBuffer.put(NODE_HEAD);
        }
        byteBuffer.putInt(hash.length);
        byteBuffer.put(hash);
        return byteBuffer.array();
    }
}