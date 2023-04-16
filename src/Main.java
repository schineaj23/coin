import java.nio.ByteBuffer;
import java.security.*;
import java.util.Base64;

public class Main {
    public static void initialTransactionTest() {
        Transaction firstTransaction = new Transaction();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

            // Create the "original owner" keyPair for the transaction
            KeyPair ownerZeroKeyPair = keyGen.generateKeyPair();

            // Create the new owner (who we want to give the coin to)
            // Add the newOwner to the transaction
            KeyPair ownerOneKeyPair = keyGen.generateKeyPair();
            PrivateKey ownerOnePrivateKey = ownerOneKeyPair.getPrivate();
            PublicKey ownerOnePublicKey = ownerOneKeyPair.getPublic();

            System.out.println("Creating dummy public key");
            firstTransaction.newOwnerKey = ownerOnePublicKey;
            firstTransaction.amount = 1;
            firstTransaction.timestampHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
            md.reset();

            // Sign the transaction (the hash of newOwner and the previous transaction) with the oldOwner private key
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(ownerZeroKeyPair.getPrivate());

            // Now hash the new owner's public key and previous transaction
            // Since this is the first transaction on the block I'm just going to hash the time again lmfao
            System.out.println("Updating signature with hash of transaction");

            // Create a buffer that contains the newOwner public key and the hash of previous transaction.
            byte[] ownerOnePublicKeyEncoded = ownerOnePublicKey.getEncoded();
            // again, the previousTransactionHash is just going to be the time for now because this is our first transaction
            byte[] previousTransactionHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
            md.reset();

            // Create the new buffer for signing
            byte[] pkPreviousBuffer = Util.concatenateBuffers(previousTransactionHash, ownerOnePublicKeyEncoded);

            // Hash these two together before signing
            byte[] hashedPreviousBuffer = md.digest(pkPreviousBuffer);
            md.reset();

            // Now sign the transaction with the oldOwner's private key
            // This is our "verification" that the oldOwner is signing their coin to the newOwner
            sig.update(hashedPreviousBuffer);
            byte[] signature = sig.sign();
            System.out.println("Signed transaction");
            System.out.printf("Transaction signature (Base64): %s\n", Base64.getEncoder().encodeToString(signature));

            // Add this signature to the transaction
            firstTransaction.oldOwnerSignature = signature;

            // Great, now that we have our first transaction, let's try making another one and verifying it!
            // Creating another keyPair for ownerTwo
            KeyPair ownerTwoKeyPair = keyGen.generateKeyPair();
            Transaction secondTransaction = new Transaction();
            secondTransaction.newOwnerKey = ownerTwoKeyPair.getPublic();
            secondTransaction.amount = 1;
            secondTransaction.timestampHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
            md.reset();

            // Let's now actually hash and sign it, verifying the signature with our previous owner!

            // Create a buffer that contains the newOwner public key and the hash of previous transaction.
            byte[] thirdOwnerPublicKey = secondTransaction.newOwnerKey.getEncoded();
            byte[] transactionHash = firstTransaction.hash();

            // Create the new buffer for signing
            pkPreviousBuffer = Util.concatenateBuffers(transactionHash, thirdOwnerPublicKey);
            
            // Hash the two together
            byte[] secondTransactionPreviousBuffer = md.digest(pkPreviousBuffer);
            md.reset();

            // Initialize the signature for second transaction :D
            Signature secondTransactionSignature = Signature.getInstance("SHA256withRSA");

            // Initialize the signature with the private key of the previous owner (ownerOne)
            secondTransactionSignature.initSign(ownerOnePrivateKey);
            
            // Add the hash of (previousTransaction + third owner public key) as the data for our signature
            secondTransactionSignature.update(secondTransactionPreviousBuffer);

            // Sign it and put it as data for our transaction
            byte[] secondTransactionSignatureBuffer = secondTransactionSignature.sign();
            secondTransaction.oldOwnerSignature = secondTransactionSignatureBuffer;

            // Verify this signature with ownerOne (this should pass)
            secondTransactionSignature.initVerify(ownerOnePublicKey);

            // NOTE: sign() and initVerify() resets the signature object, so you MUST update with the buffer before verifying 
            secondTransactionSignature.update(secondTransactionPreviousBuffer);
            System.out.printf("Owner1 Second Transaction verify (should pass): %s\n", (secondTransactionSignature.verify(secondTransactionSignatureBuffer)) ? "PASS" : "FAIL");
            

            // Verify this signature with ownerZero (this should fail)
            secondTransactionSignature.initVerify(ownerZeroKeyPair.getPublic());
            secondTransactionSignature.update(secondTransactionPreviousBuffer);
            System.out.printf("Owner0 Second Transaction verify (should fail): %s\n", (secondTransactionSignature.verify(secondTransactionSignatureBuffer)) ? "PASS" : "FAIL");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static KeyPair GOD_KEY_PAIR;

    // Debug function to give coins to someone, remove this when the network is really working
    public static Transaction mintCoinDebug(User recipient, double amount) {
        System.out.printf("Attempting to mint %.2f coins for user %s\n", amount, recipient.getName());
        Transaction t = new Transaction();
        t.amount = amount;
        t.newOwnerKey = recipient.getPublicKey();

        try {
            Signature dummySignature = Signature.getInstance("SHA256withRSA");
            dummySignature.initSign(GOD_KEY_PAIR.getPrivate());
            dummySignature.update(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());

            // God cares not about transactions, sign the time instead
            t.oldOwnerSignature = dummySignature.sign();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // FIXME: timestamp bug
        t.timestampHash = Util.hashBuffer(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());

        return t;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        GOD_KEY_PAIR = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        User bobby = new User("bobby");
        User jimmy = new User("jimmy");
        
        // Mint a coin for bobby
        Transaction mintTransaction = mintCoinDebug(bobby, 1);

        // Have bobby send jimmy some moneys
        bobby.send(mintTransaction, jimmy, 0.5);

        System.out.println("Did it work? idk bobby should now have 0.5");
    }
}