package com.asch.coin;

import java.nio.ByteBuffer;

// The transaction "input" in essence is a reference to a previous output
// Such that you could trace back an input to an output statement from the block in which it was mined!
public class TransactionInput extends Hashable {
    public TransactionInput() {
    }

    public TransactionInput(TransactionId previousTransaction, int associatedOutput, byte[] signature) {
        this.previousTransaction = previousTransaction;
        this.previousOutput = associatedOutput;
        this.signatureSize = signature.length;
        this.signature = signature;
    }

    TransactionId previousTransaction;
    int previousOutput; // Index of the output from the previous transaction (used for verification)
    int signatureSize;
    byte[] signature; // Signature used to verify the transaction so ownership can be transferred

    @Override
    public String toString() {
        return String.format("""
                PreviousTransactionTXID: %s
                PreviousTransactionOutput: %d
                SignatureSize: %d
                Signature: %s
                """, (previousTransaction == null) ? "COINBASE" : previousTransaction, previousOutput, signatureSize,
                Util.bytesToHex(signature));
    }

    @Override
    public int getSerializedSize() {
        // TransactionID (32 bytes) + associatedOutput (4 bytes, int)
        // + signatureSize (4 bytes, int) + signature (signatureSize bytes)
        return 32 + 4 + 4 + signatureSize;
    }

    @Override
    public ByteBuffer serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.put(previousTransaction.get());
        buffer.putInt(previousOutput).putInt(signatureSize);
        buffer.put(signature);
        return buffer;
    }
}
