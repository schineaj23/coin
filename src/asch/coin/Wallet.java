package asch.coin;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;

public class Wallet {
    private KeyPair keyPair;
    private final String name; // for the sake of user-friendliness
    private final ArrayList<UnspentTransactionOutput> outputs = new ArrayList<>();

    public Wallet(String name) {
        this.name = name;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            this.keyPair = keyGen.generateKeyPair();
        } catch(Exception e) {
            System.out.printf("Could not create user %s!\n", name);
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public double getBalance() {
        double balance = 0;
        for(UnspentTransactionOutput utxo : outputs) {
            balance += utxo.output.amount;
        }
        return balance;
    }

    private byte[] signTransaction(byte[] bufferToSign) {
        try {
            Signature transactionSignature = Signature.getInstance("SHA256withRSA");
            transactionSignature.initSign(keyPair.getPrivate());
            transactionSignature.update(bufferToSign);
            return transactionSignature.sign(); 
        } catch(Exception e) {
            System.out.printf("User (%s) could not sign transaction!\n", name);
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifyTransaction(byte[] transactionBuffer, byte[] signatureBuffer) {
        try {
            Signature transactionSignature = Signature.getInstance("SHA256withRSA");
            transactionSignature.initVerify(keyPair.getPublic());
            transactionSignature.update(transactionBuffer);
            return transactionSignature.verify(signatureBuffer); 
        } catch(Exception e) {
            System.out.printf("User (%s) could not verify transaction!\n", name);
            e.printStackTrace();
            return false;
        }
    }

    // Remove any UTXOs that are no longer valid.
    public void verifyWallet() {
        // TODO: implement
    }

    // Using the "Pay to Public Key" paradigm, returns "True" if successfully created transaction
    public boolean send(PublicKey recipient, double amount, Transaction out) {
        double currentBalance = getBalance();
        if(currentBalance <= amount) {
            System.out.printf("Wallet \"%s\" has insufficient coins to spend! Balance: %.2f, Amount: %.2f\n", name, currentBalance, amount);
            return false;
        }
        Transaction transaction = new Transaction(); // Create transaction object to hold our data

        // Find UTXOs we can use for our inputs
        ArrayList<UnspentTransactionOutput> utxoToUse = new ArrayList<>();
        double coinsAvailable = 0;
        while(coinsAvailable < amount && !outputs.isEmpty()) {
            utxoToUse.add(outputs.get(0));
            coinsAvailable += outputs.get(0).output.amount;
            outputs.remove(0); // remove the used outputs, we will make a new one later
        }
        
        // Now use our UTXOs as inputs to our new transaction
        double change = 0;
        int i = 0;
        while(coinsAvailable > amount) {
            UnspentTransactionOutput utxo = utxoToUse.get(i);
            TransactionInput input = new TransactionInput();
            input.previousOutput = utxo.outputId;
            input.previousTransaction = utxo.associatedTransaction;

            // Now add our signature! We can sign whatever, as long as it can be verified by someone else
            // Signing the old transaction so that we know what to compare against, and that thing always changes
            // https://cryptobook.nakov.com/digital-signatures/rsa-signatures
            byte[] signature = signTransaction(utxo.associatedTransaction.get());
            input.signatureSize = signature.length;
            input.signature = signature;

            transaction.addInput(input);

            coinsAvailable -= utxo.output.amount;
            if(coinsAvailable < amount) {
                change = coinsAvailable;
                break;
            }
            i++;
        }

        // After adding all of our inputs, create a new output for our recipient containing the amount
        transaction.addOutput(new TransactionOutput(amount, recipient.getEncoded()));
        if(change > 0) {
            // Send back the change to ourselves, we do this so that there is a public record of our balance
            TransactionOutput output = new TransactionOutput(change, keyPair.getPublic().getEncoded());
            transaction.addOutput(output);

            // We KNOW this UTXO is unspent, so add it to our local list of UTXOs for spending later
            outputs.add(new UnspentTransactionOutput(transaction.getTransactionId(), i, output));
        }

        // Copy the transaction to the output parameter
        out = transaction;
        return true;
    }
}
