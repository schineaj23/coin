package asch.coin;
import java.nio.ByteBuffer;
import java.util.ArrayList;

// This part is following the Bitcoin spec from the bitcoin white paper (Transactions section)
// For the moment is mainly just a struct that contains the data for a transaction
public class Transaction extends Hashable {
    // TODO: have the timestamp inherit from the block it is being added to. just realized this damn im kinda dumb
    // at least i think this is how it works???
    public byte[] timestampHash;

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

    @Override
    public String toString() {
        String ret = String.format("=============START TRANSACTION=============\n");
        ret += "TXID: " + getTransactionId() + "\n";
        ret = String.format("=============START INPUTS=============\nInputs: %d\n", inputs.size());
        for(TransactionInput input : inputs) {
            ret += input + "\n";
        }
        ret += String.format("=============END INPUTS=============\n");
        ret += String.format("=============START OUTPUTS=============\nOutputs: %d\n", outputs.size());
        for(TransactionOutput output : outputs) {
            ret += output + "\n";
        }
        ret += "=============END OUTPUTS=============\n=============END TRANSACTION=============\n";
        return ret;
    }

    @Override
    public int getSerializedSize() {
        // input count (int, 4 bytes) + output count (int, 4 bytes)
        int size = 4 + 4; 
        for(TransactionInput in : inputs) {
            size += in.getSerializedSize();
        }
        for(TransactionOutput out : outputs) {
            size += out.getSerializedSize();
        }
        return size;
    }

    @Override
    public ByteBuffer serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.putInt(inputs.size());
        for(TransactionInput in : inputs) {
            buffer.put(in.serialize());
        }
        buffer.putInt(outputs.size());
        for(TransactionOutput out : outputs) {
            buffer.put(out.serialize());
        }
        return buffer;
    }
}
