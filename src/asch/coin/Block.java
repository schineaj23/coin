package asch.coin;
import asch.coin.tree.MerkleTree;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Block extends Hashable {
    private final ArrayList<Transaction> transactions = new ArrayList<>();

    public byte[] timestampHash;
    private int timestamp; // this is internal to the class

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

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // Returns the index of the transaction for transactionId if exists. Returns -1 if it does not exist.
    public int indexOf(TransactionId a) {
        for(int i=0;i<transactions.size();i++) {
            if(a.compareTo(transactions.get(i).getTransactionId()) == 0)
                return i;
        }
        return -1;
    }

    public Transaction getRootTransaction() {
        if(transactions.size() < 1) {
            System.out.println("Block::getRootTransaction(): transactions empty!");
            return null;
        }
        return transactions.get(0);
    }

    // FIXME: timestamp bug
    public void generateTimestamp() {
        // Yes, I do realize that this loses precision, doesn't really matter though it's MY CURRENCY, MY AMERICA!
        timestamp = (int)(System.currentTimeMillis() / 1000L);
        timestampHash = Util.hashBuffer(ByteBuffer.allocate(8).putLong(timestamp).array());
    }

    public ByteBuffer getBlockHeader() {
        if(merkleRoot == null) {
            merkleRoot = new MerkleTree().generateTree(transactions).hash();
        }
        
        if(timestampHash == null) {
            generateTimestamp();
        }

        int transactionsSize = getTransactionsSerializedSize();

        // previousBlock (32 bytes) + merkleRoot (32 bytes) 
        // + timestamp (4 bytes) + size (4 bytes) + nonce (4 bytes) = 76 bytes
        ByteBuffer buffer = ByteBuffer.allocate(32 + 32 + 4 + 4 + 4);
        buffer.put(previousBlockHash).put(merkleRoot);
        buffer.putInt(timestamp).putInt(transactionsSize).putInt(nonce);

        return buffer;
    }

    @Override
    public byte[] hash() {
        // Block Hash = hash(hash(block header))
        // Similar to how transactionId = hash(hash(transaction))
        return Util.hashBuffer(Util.hashBuffer(getBlockHeader().array()));
    }

    private int getTransactionsSerializedSize() {
        int transactionsSize = 0;
        for(Transaction t : transactions) {
            transactionsSize += t.getSerializedSize();
        }
        // Hopefully there aren't so many transactions such that we overflow! (surely)
        return transactionsSize;
    }

    @Override
    public int getSerializedSize() {
        // Size = header (76 bytes) + transactions (transactionSize bytes)
        int transactionSize = getTransactionsSerializedSize();
        return 76 + transactionSize;
    }

    @Override
    public ByteBuffer serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.put(getBlockHeader());
        for(Transaction t : transactions) {
            buffer.put(t.serialize());
        }
        return buffer;
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