package com.asch.coin;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;

public class Wallet {
    private KeyPair keyPair;
    private final String name; // for the sake of user-friendliness
    private final ArrayList<UnspentTransactionOutput> userUnspentOutputs = new ArrayList<>();

    public Wallet(String name) {
        this.name = name;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            this.keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            System.out.printf("Could not create user %s!\n", name);
            e.printStackTrace();
        }

        // Build wallet to start.
        rebuildWallet();
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public double getBalance() {
        double balance = 0;
        for (UnspentTransactionOutput utxo : userUnspentOutputs) {
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
        } catch (Exception e) {
            System.out.printf("Wallet (%s) could not sign transaction!\n", name);
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
        } catch (Exception e) {
            System.out.printf("User (%s) could not verify transaction!\n", name);
            e.printStackTrace();
            return false;
        }
    }

    public void rebuildWallet() {
        // O(N^3) moment
        userUnspentOutputs.clear();
        for(UnspentTransactionOutput utxo : Blockchain.getInstance().getUnspentTransactionOutputs()) {
            if(Util.bufferEquality(utxo.output.destinationPublicKey, getPublicKey().getEncoded())) {
                userUnspentOutputs.add(utxo);
            }
        }

        getBalance();
        Collections.sort(userUnspentOutputs);

        System.out.printf("Wallet::rebuildWallet() complete for %s. Summary: %s\n", name, this);
    }

    // Using the "Pay to Public Key" paradigm, returns "True" if successfully
    // created transaction
    public Transaction send(PublicKey recipient, double amount) {
        // Before we try and spend anything, rebuild our wallet.
        rebuildWallet();

        double currentBalance = getBalance();
        if (currentBalance <= amount) {
            throw new RuntimeException(
                    String.format("Wallet \"%s\" has insufficient coins to spend! Balance: %.2f, Amount: %.2f\n", name,
                            currentBalance, amount));
        }
        Transaction transaction = new Transaction(); // Create transaction object to hold our data

        // Find UTXOs we can use for our inputs
        ArrayList<UnspentTransactionOutput> utxoToUse = new ArrayList<>();
        double coinsAvailable = 0;
        while (coinsAvailable < amount && !userUnspentOutputs.isEmpty()) {
            UnspentTransactionOutput utxo = userUnspentOutputs.get(0);

            utxoToUse.add(utxo);
            coinsAvailable += utxo.output.amount;
            System.out.printf("Selecting transaction amount: %.2f, totalamt: %.2f, available %.2f\n",
                    utxo.output.amount, amount, coinsAvailable);

            userUnspentOutputs.remove(0); // remove the used outputs, we will make a new one later
        }

        double change = coinsAvailable - amount;

        // Now use our UTXOs as inputs to our new transaction
        int i = 0;
        while (coinsAvailable > amount) {
            UnspentTransactionOutput utxo = utxoToUse.get(i);

            if (!Blockchain.getInstance().unlockAndSpendOutput(utxo, keyPair.getPrivate())) {
                throw new RuntimeException(
                        "Wallet::send(): unlockAndSpendOutput() not authorized! How did we get this UTXO?");
            }

            TransactionInput input = new TransactionInput();
            input.previousOutput = utxo.outputId;
            input.previousTransaction = utxo.associatedTransaction;

            // Now add our signature! We can sign whatever, as long as it can be verified by
            // someone else
            // Signing the old transaction so that we know what to compare against, and that
            // thing always changes
            // https://cryptobook.nakov.com/digital-signatures/rsa-signatures
            byte[] signature = signTransaction(utxo.associatedTransaction.get());
            input.signatureSize = signature.length;
            input.signature = signature;

            transaction.addInput(input);

            coinsAvailable -= utxo.output.amount;
            i++;
        }

        // After adding all of our inputs, create a new output for our recipient
        // containing the amount
        
        TransactionOutput recipientTransaction = new TransactionOutput(amount, recipient.getEncoded());
        transaction.addOutput(recipientTransaction);
        if (change > 0) {
            // Send back the change to ourselves, we do this so that there is a public
            // record of our balance
            TransactionOutput output = new TransactionOutput(change, keyPair.getPublic().getEncoded());
            transaction.addOutput(output);

            // We KNOW this UTXO is unspent, so add it to our local list of UTXOs for
            // spending later
            TransactionId transactionId = transaction.getTransactionId();

            UnspentTransactionOutput utxo = new UnspentTransactionOutput();
            utxo.associatedTransaction = transactionId;
            utxo.output = output;
            utxo.outputId = 1;

            Blockchain.getInstance().addUnspentTransactionOutput(utxo);
        }
        Blockchain.getInstance().addUnspentTransactionOutput(new UnspentTransactionOutput(transaction.getTransactionId(), 0, recipientTransaction));

        // System.out.println(transaction);
        Collections.sort(userUnspentOutputs); // Sort outputs in wallet after removing used UTXOs
        return transaction;
    }

    public Transaction send(Wallet recipient, double amount) {
        return send(recipient.getPublicKey(), amount);
    }

    @Override
    public String toString() {
        String transactions = "\n";
        for (UnspentTransactionOutput utxo : userUnspentOutputs) {
            transactions += "PrevTXID: " + utxo.associatedTransaction + " " + utxo.output.amount + "\n";

        }
        return String.format("""
                Name: %s
                Balance: %.2f
                PublicKey (hash): %s
                Transactions: (count %d) %s""", name, getBalance(),
                Util.bytesToHex(Util.hashBuffer(getPublicKey().getEncoded())),
                userUnspentOutputs.size(),
                transactions);
    }
}
