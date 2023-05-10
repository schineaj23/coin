package asch.coin;

// This structure is used only by the wallet to keep track of itself
public class UnspentTransactionOutput implements Comparable<UnspentTransactionOutput> {
    public UnspentTransactionOutput() {}
    
    public UnspentTransactionOutput(TransactionId associatedTransaction, int outputId, TransactionOutput output) {
        this.associatedTransaction = associatedTransaction;
        this.outputId = outputId;
        this.output = output;
    }

    TransactionId associatedTransaction;
    int outputId; // The output id from associatedTransaction
    TransactionOutput output;

    @Override
    public int compareTo(UnspentTransactionOutput other) {
        double delta = output.amount - other.output.amount;
        if(delta > 0.0001)
            return 1;
        if(delta < -0.0001)
            return -1;
        return 0;
    }
}
