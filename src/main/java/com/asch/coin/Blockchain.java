package com.asch.coin;

import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Blockchain {
    private static Blockchain chainInternal;

    public static Blockchain getInstance() {
        if (chainInternal == null) {
            chainInternal = new Blockchain();
        }
        return chainInternal;
    }

    private ArrayList<Block> blocks = new ArrayList<>();
    private ArrayList<UnspentTransactionOutput> unspentOutputsPool = new ArrayList<>();

    public void addBlock(Block block) {
        if (!Proof.isBlockValid(block)) {
            System.out.printf("Blockchain::addBlock(): Block (%s) invalid!\n", Util.bytesToHex(block.hash()));
            return;
        }

        if (blocks.isEmpty()) {
            blocks.add(block);
            // System.out.printf("Blockchain::addBlock(): Added block %s to blockchain\n",
            // Util.bytesToHex(block.hash()));
            return;
        }

        // Make sure that this is actually a "chain" (new block MUST start at end of
        // last block)
        if (blocks.contains(block)
                || !Util.bufferEquality(blocks.get(blocks.size() - 1).hash(), block.previousBlockHash))
            return;

        blocks.add(block);
        // System.out.printf("Blockchain::addBlock(): Added block %s to blockchain\n",
        // Util.bytesToHex(block.hash()));
    }

    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public Block getMostRecentBlock() {
        return blocks.get(blocks.size()-1);
    }

    public List<UnspentTransactionOutput> getUnspentTransactionOutputs() {
        return Collections.unmodifiableList(unspentOutputsPool);
    }

    public void addUnspentTransactionOutput(UnspentTransactionOutput utxo) {
        if (unspentOutputsPool.contains(utxo))
            return;
        unspentOutputsPool.add(utxo);
    }

    public boolean unlockAndSpendOutput(UnspentTransactionOutput utxo, PrivateKey privateKey) {
        // First sign the data with our private key, then verify it with the public key
        // from the transaction
        // If this is successful, then we have "unlocked" the transaction and can spend
        // (remove) the UTXO
        try {
            Signature signatureInterface = Signature.getInstance("SHA256withRSA");
            signatureInterface.initSign(privateKey);
            signatureInterface.update(utxo.associatedTransaction.get());
            byte[] signature = signatureInterface.sign();

            Signature verifyInterface = Signature.getInstance("SHA256withRSA");
            verifyInterface.initVerify(Util.keyFromBuffer(utxo.output.destinationPublicKey));
            verifyInterface.update(utxo.associatedTransaction.get());
            if (!verifyInterface.verify(signature))
                return false;

            // If the verify = true, this means that the TX signature (with our key)
            // matches the public key from block
            // Therefore we can sign it away to the new owner (the public key), and discard
            // the UTXO from our pool.
            unspentOutputsPool.remove(utxo);
        } catch (Exception e) {
            System.out.println("Blockchain::unlockAndSpendOutput() failed!");
            e.printStackTrace();
        }

        return true;
    }
}
