package asch.coin;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This part is following the Bitcoin spec from the bitcoin white paper (Transactions section)
// For the moment is mainly just a struct that contains the data for a transaction
public class Transaction extends Hashable {
    private ArrayList<TransactionInput> inputs = new ArrayList<>();

    private ArrayList<TransactionOutput> outputs = new ArrayList<>();

    public TransactionId getTransactionId() {
        return new TransactionId(this);
    }

    public void addInput(TransactionInput input) {
        inputs.add(input);
    }

    public void addOutput(TransactionOutput output) {
        outputs.add(output);
    }

    // Ensure that the inputs we get CANNOT be modified.
    // We wouldn't want to accidentally mess up the transaction
    public List<TransactionInput> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<TransactionOutput> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    @Override
    public String toString() {
        String ret = String.format("=============START TRANSACTION=============\n");
        ret += "TXID: " + getTransactionId() + "\n";
        ret += String.format("=============START INPUTS=============\nInputs: %d\n", inputs.size());
        for (TransactionInput input : inputs) {
            ret += input + "\n";
        }
        ret += String.format("=============END INPUTS=============\n");
        ret += String.format("=============START OUTPUTS=============\nOutputs: %d\n", outputs.size());
        for (TransactionOutput output : outputs) {
            ret += output + "\n";
        }
        ret += "=============END OUTPUTS=============\n=============END TRANSACTION=============";
        return ret;
    }

    @Override
    public int getSerializedSize() {
        // input count (int, 4 bytes) + output count (int, 4 bytes)
        int size = 4 + 4;
        for (TransactionInput in : inputs) {
            size += in.getSerializedSize();
        }
        for (TransactionOutput out : outputs) {
            size += out.getSerializedSize();
        }
        return size;
    }

    @Override
    public ByteBuffer serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.putInt(inputs.size());
        for (TransactionInput in : inputs) {
            buffer.put(in.serialize());
        }
        buffer.putInt(outputs.size());
        for (TransactionOutput out : outputs) {
            buffer.put(out.serialize());
        }
        return buffer;
    }
}
