// The transaction "input" in essence is a reference to a previous output
// Such that you could trace back an input to an output statement from the block in which it was mined!
public class TransactionInput extends Hashable {
    public TransactionInput() {}

    public TransactionInput(TransactionId associatedTransaction, int associatedOutput, byte[] signature) {
        this.associatedTransaction = associatedTransaction;
        this.associatedOutput = associatedOutput;
        this.signatureSize = signature.length;
        this.signature = signature;
    }

    TransactionId associatedTransaction;
    int associatedOutput; // Index of the output from the previous transaction (used for verification) 
    int signatureSize;
    byte[] signature; // Signature used to verify the transaction so ownership can be transferred

    @Override
    public String toString() {
        return String.format("""
                PreviousTransactionTXID: %s
                PreviousTransactionOutput: %d
                SignatureSize: %d
                Signature: %s
                """, associatedTransaction, associatedOutput, signatureSize, Util.bytesToHex(signature));
    }
}
