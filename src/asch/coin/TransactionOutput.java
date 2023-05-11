package asch.coin;

import java.nio.ByteBuffer;

public class TransactionOutput extends Hashable {
    public TransactionOutput() {
    }

    public TransactionOutput(double amount, byte[] destinationPublicKey) {
        this.amount = amount;
        this.destinationPublicKeySize = destinationPublicKey.length;
        this.destinationPublicKey = destinationPublicKey;
    }

    double amount;
    int destinationPublicKeySize;
    byte[] destinationPublicKey;

    @Override
    public String toString() {
        return String.format("""
                Amount: %.3f
                DestinationPublicKeySize: %d
                DestinationPublicKey(Hash): %s
                """, amount, destinationPublicKeySize, Util.bytesToHex(Util.hashBuffer(destinationPublicKey)));
    }

    @Override
    public int getSerializedSize() {
        // amount (double, 8 bytes) + publicKeySize (int, 4 bytes) + publicKey
        // (publicKeySize bytes)
        return 8 + 4 + destinationPublicKeySize;
    }

    @Override
    public ByteBuffer serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.putDouble(amount);
        buffer.putInt(destinationPublicKeySize);
        buffer.put(destinationPublicKey);
        return buffer;
    }
}
