package com.asch.coin;

import java.io.Serializable;

// Wraps the TXID in an object so that it's not just a byte buffer which may get messed up
public class TransactionId implements Serializable, Comparable<TransactionId> {
    private final byte[] bufferInternal;

    public TransactionId() {
        bufferInternal = new byte[32];
        for(byte b : bufferInternal) {
            b = 0x00;
        }
    }

    public TransactionId(Transaction transaction) {
        bufferInternal = Util.hashBuffer(transaction.hash());
    }

    public byte[] get() {
        return bufferInternal;
    }

    @Override
    public int compareTo(TransactionId other) {
        return Util.bufferEquality(bufferInternal, other.bufferInternal) ? 0 : 1;
    }

    @Override
    public String toString() {
        return Util.bytesToHex(bufferInternal);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransactionId) {
            return Util.bufferEquality(bufferInternal, ((TransactionId) obj).bufferInternal);
        }
        return obj == this;
    }
}