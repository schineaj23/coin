package asch.coin.tree;

import asch.coin.Hashable;
import asch.coin.Transaction;
import asch.coin.Util;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;

/*  Resources for this:
    https://en.wikipedia.org/wiki/Merkle_tree
    Shamelessly stole code from: https://www.pranaybathini.com/2021/05/merkle-tree.html
    https://github.com/quux00/merkle-tree/blob/master/README.md
    */
public class MerkleTree extends Hashable {
    private static byte[] TREE_HEAD = { (byte) 0xDE, (byte) 0xAD };
    private int numNodes = 0;
    private MerkleNode parentNode;

    public MerkleNode generateTree(Collection<Transaction> transactionList) {
        ArrayList<MerkleNode> nodes = new ArrayList<>();
        for (Transaction t : transactionList) {
            nodes.add(new MerkleNode(null, null, t.hash()));
        }
        return buildTree(nodes);
    }

    private MerkleNode buildTree(ArrayList<MerkleNode> children) {
        ArrayList<MerkleNode> parents = new ArrayList<>();
        while (children.size() != 1) {
            int index = 0, length = children.size();
            while (index < length) {
                MerkleNode leftChild = children.get(index);
                MerkleNode rightChild = null;
                if ((index + 1) < length) {
                    rightChild = children.get(index + 1);
                } else {
                    rightChild = new MerkleNode(null, null, leftChild.hash());
                }
                byte[] parentHash = Util.hashBuffer(Util.concatenateBuffers(leftChild.hash(), rightChild.hash()));
                parents.add(new MerkleNode(leftChild, rightChild, parentHash));
                index += 2;
                numNodes += 3; // The parent node + 2 children nodes (even if their contents are null)
            }
            children = parents;
            parents = new ArrayList<>();
        }
        parentNode = children.get(0);
        System.out.printf("MerkleTree NumNodes: %d\n", numNodes);
        return children.get(0);
    }

    @Override
    public byte[] hash() {
        if (parentNode == null) {
            throw new RuntimeException("Cannot hash() merkleTree, parentNode is null!");
        }
        return parentNode.hash();
    }

    @Override
    public int getSerializedSize() {
        // 0xDEAD (2 bytes for header) 4 bytes (length, int)
        // 2 byte (type, byte) 4 bytes (length, int) 32 bytes (hash, byte) = 38 per node
        return numNodes * (2 + 4 + 32) + 4 + 2;
    }

    @Override
    public ByteBuffer serialize() {
        ArrayDeque<MerkleNode> queue = new ArrayDeque<>(numNodes / 2 + 1);
        if (parentNode == null) {
            System.out.println("MerkleTree::serializeTree() parent node == null!");
            return null;
        }
        queue.add(parentNode);
        ByteBuffer buf = ByteBuffer.allocate(getSerializedSize());
        System.out.println("buffer limit " + buf.limit());
        buf.put(TREE_HEAD);
        buf.putInt(numNodes);
        while (!queue.isEmpty()) {
            MerkleNode node = queue.remove();
            ByteBuffer serialized = node.serialize();
            // System.out.println(serialized.limit());
            buf.put(serialized);
            if (node.getLeft() != null)
                queue.add(node.getLeft());
            if (node.getRight() != null)
                queue.add(node.getRight());
        }
        return buf;
    }
}
