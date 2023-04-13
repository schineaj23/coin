import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

public class Main {
     public static void main(String[] args) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

        CoinTransaction firstTransaction = new CoinTransaction();
        try {
            // Create the "original owner" keyPair for the transaction
            KeyPair originalKeyPair = keyGen.generateKeyPair();

            // Create the new owner (who we want to give the coin to)
            // Add the newOwner to the transaction
            KeyPair newKeyPair = keyGen.generateKeyPair();
            System.out.println("Creating dummy public key");
            firstTransaction.newOwnerKey = newKeyPair.getPublic();
            firstTransaction.amount = 1;
            firstTransaction.timestampHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());

            // Sign the transaction (the hash of newOwner and the previous transaction) with the oldOwner private key
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(originalKeyPair.getPrivate());

            // Now hash the new owner's public key and previous transaction
            // Since this is the first transaction on the block I'm just going to hash the time again lmfao
            System.out.println("Updating signature with hash of transaction");

            // Create a buffer that contains the newOwner public key and the hash of previous transaction.
            byte[] newOwnerPublicKey = newKeyPair.getPublic().getEncoded();
            // again, the previousTransactionHash is just going to be the time for now because this is our first transaction
            byte[] previousTransactionHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());

            // Create the new buffer for signing
            byte[] pkPreviousBuffer = new byte[newOwnerPublicKey.length + previousTransactionHash.length];
            System.arraycopy(newOwnerPublicKey, 0, pkPreviousBuffer, 0, newOwnerPublicKey.length);
            System.arraycopy(previousTransactionHash, 0, pkPreviousBuffer, newOwnerPublicKey.length, previousTransactionHash.length);

            // Hash these two together before signing
            byte[] hashedPreviousBuffer = md.digest(pkPreviousBuffer);

            // Now sign the transaction with the oldOwner's private key
            // This is our "verification" that the oldOwner is signing their coin to the newOwner
            sig.update(hashedPreviousBuffer);
            byte[] signature = sig.sign();
            System.out.println("Signed transaction");
            System.out.printf("Transaction signature (Base64): %s\n", Base64.getEncoder().encodeToString(signature));

            // Add this signature to the transaction
            firstTransaction.oldOwnerSignature = signature;

            // Great, now that we have our first transaction, let's try making another one and verifying it!
            // Creating another keyPair for owner3
            KeyPair owner3KeyPair = keyGen.generateKeyPair();
            CoinTransaction secondTransaction = new CoinTransaction();
            secondTransaction.newOwnerKey = owner3KeyPair.getPublic();
            secondTransaction.amount = 1;
            secondTransaction.timestampHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());

            // Let's now actually hash and sign it, verifying the signature with our previous owner!

            // Create a buffer that contains the newOwner public key and the hash of previous transaction.
            byte[] thirdOwnerPublicKey = secondTransaction.newOwnerKey.getEncoded();
            byte[] transactionHash = firstTransaction.hash();

            // Create the new buffer for signing
            pkPreviousBuffer = new byte[thirdOwnerPublicKey.length + transactionHash.length];
            System.arraycopy(thirdOwnerPublicKey, 0, pkPreviousBuffer, 0, thirdOwnerPublicKey.length);
            System.arraycopy(transactionHash, 0, pkPreviousBuffer, thirdOwnerPublicKey.length, transactionHash.length);
            // Hash the two together
            byte[] secondTransactionPreviousBuffer = md.digest(pkPreviousBuffer);

            // Initialize the signature for second transaction :D
            Signature secondTransactionSignature = Signature.getInstance("SHA256withRSA");

            // TODO: verify previous transaction and sign this current transaction!


        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}