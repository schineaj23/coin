package asch.coin.tree;

import asch.coin.Hashable;

public class MerkleNode extends Hashable {
    byte[] hash;
    MerkleElement left;
    MerkleElement right;
}