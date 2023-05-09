package asch.coin;
public class TransactionOutput extends Hashable {
    public TransactionOutput() {}

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
                DestinationPublicKey: %s
                """, amount, destinationPublicKeySize, Util.bytesToHex(destinationPublicKey));
    }
}
