package com.asch.coin.tree;

import java.nio.ByteBuffer;

import com.asch.coin.Hashable;

public class MerkleNode extends Hashable {
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

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public byte[] hash() {
        return hash;
    }

    @Override
    public int getSerializedSize() {
        // Head (2 bytes) + Length (4 bytes, int) + Hash (32 bytes)
        return 2 + 4 + 32;
    }

    @Override
    public ByteBuffer serialize() {
        System.out.println("Node hash length: " + hash.length);
        ByteBuffer byteBuffer = ByteBuffer.allocate(getSerializedSize());
        // Leaf has a different head (0xFEFE)
        if (isLeaf()) {
            byte[] LEAF_HEAD = { (byte) 0xFE, (byte) 0xFE };
            byteBuffer.put(LEAF_HEAD);
        } else {
            byte[] NODE_HEAD = { (byte) 0xBE, (byte) 0xAD };
            byteBuffer.put(NODE_HEAD);
        }
        byteBuffer.putInt(hash.length);
        byteBuffer.put(hash);
        return byteBuffer;
    }
}