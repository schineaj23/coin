package asch.coin;
import java.util.ArrayList;

// This part is following the Bitcoin spec from the bitcoin white paper (Transactions section)
// For the moment is mainly just a struct that contains the data for a transaction
public class Transaction extends Hashable {
    // TODO: have the timestamp inherit from the block it is being added to. just realized this damn im kinda dumb
    // at least i think this is how it works???
    public byte[] timestampHash;

    private int inputCount = 0;
    private ArrayList<TransactionInput> inputs = new ArrayList<>();

    private int outputCount = 0;
    private ArrayList<TransactionOutput> outputs = new ArrayList<>();

    public TransactionId getTransactionId() {
        return new TransactionId(this);
    }

    public void addInput(TransactionInput input) {
        inputs.add(input);
        inputCount++;
    }

    public void addOutput(TransactionOutput output) {
        outputs.add(output);
        outputCount++;
    }

    @Override
    public String toString() {
        String ret = String.format("=============START TRANSACTION=============\n");
        ret += "TXID: " + getTransactionId() + "\n";
        ret = String.format("=============START INPUTS=============\nInputs: %d\n", inputCount);
        for(TransactionInput input : inputs) {
            ret += input + "\n";
        }
        ret += String.format("=============END INPUTS=============\n");
        ret += String.format("=============START OUTPUTS=============\nOutputs: %d\n", outputCount);
        for(TransactionOutput output : outputs) {
            ret += output + "\n";
        }
        ret += "=============END OUTPUTS=============\n=============END TRANSACTION=============\n";
        return ret;
    }
}
