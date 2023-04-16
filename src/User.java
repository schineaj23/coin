import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;

public class User {
    private KeyPair userKeyPair;
    private String name;

    public User(String name) {
        this.name = name;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            this.userKeyPair = keyGen.generateKeyPair();
        } catch(Exception e) {
            System.out.printf("Could not create user %s!\n", name);
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return userKeyPair.getPublic();
    }

    private byte[] signTransaction(byte[] bufferToSign) {
        try {
            Signature transactionSignature = Signature.getInstance("SHA256withRSA");
            transactionSignature.initSign(userKeyPair.getPrivate());
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
            transactionSignature.initVerify(userKeyPair.getPublic());
            transactionSignature.update(transactionBuffer);
            return transactionSignature.verify(signatureBuffer); 
        } catch(Exception e) {
            System.out.printf("User (%s) could not verify transaction!\n", name);
            e.printStackTrace();
            return false;
        }
    }

    public Transaction send(Transaction previousTransaction, User otherUser, double amount) {
        Transaction transaction = new Transaction();
        // This is our recipient of the funds
        transaction.newOwnerKey = otherUser.getPublicKey();
        transaction.amount = amount;
        // Hash together the previous transaction and new owner public key
        byte[] transactionBuffer = Util.hashBuffer(Util.concatenateBuffers(previousTransaction.hash(), otherUser.getPublicKey().getEncoded()));

        // Sign this buffer to transfer ownership
        byte[] transactionSignature = signTransaction(transactionBuffer);

        // Add the signature to the transaction then return it
        transaction.oldOwnerSignature = transactionSignature;

        // Timestamp this transaction (this really should be done by the nodes and not the user itself, refactor this)
        // FIXME: using unix system time again for timestamping. update this so it is recieved by the node listening for it as the timestamp
        transaction.timestampHash = Util.hashBuffer(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
    
        return transaction;
    }
}
