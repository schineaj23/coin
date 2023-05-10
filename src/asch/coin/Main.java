package asch.coin;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;

public class Main {
    // public static void initialTransactionTest() {
    //     Transaction firstTransaction = new Transaction();
    //     try {
    //         MessageDigest md = MessageDigest.getInstance("SHA-256");
    //         KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

    //         // Create the "original owner" keyPair for the transaction
    //         KeyPair ownerZeroKeyPair = keyGen.generateKeyPair();

    //         // Create the new owner (who we want to give the coin to)
    //         // Add the newOwner to the transaction
    //         KeyPair ownerOneKeyPair = keyGen.generateKeyPair();
    //         PrivateKey ownerOnePrivateKey = ownerOneKeyPair.getPrivate();
    //         PublicKey ownerOnePublicKey = ownerOneKeyPair.getPublic();

    //         System.out.println("Creating dummy public key");
    //         firstTransaction.newOwnerKey = ownerOnePublicKey;
    //         firstTransaction.amount = 1;
    //         firstTransaction.timestampHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
    //         md.reset();

    //         // Sign the transaction (the hash of newOwner and the previous transaction) with the oldOwner private key
    //         Signature sig = Signature.getInstance("SHA256withRSA");
    //         sig.initSign(ownerZeroKeyPair.getPrivate());

    //         // Now hash the new owner's public key and previous transaction
    //         // Since this is the first transaction on the block I'm just going to hash the time again lmfao
    //         System.out.println("Updating signature with hash of transaction");

    //         // Create a buffer that contains the newOwner public key and the hash of previous transaction.
    //         byte[] ownerOnePublicKeyEncoded = ownerOnePublicKey.getEncoded();
    //         // again, the previousTransactionHash is just going to be the time for now because this is our first transaction
    //         byte[] previousTransactionHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
    //         md.reset();

    //         // Create the new buffer for signing
    //         byte[] pkPreviousBuffer = Util.concatenateBuffers(previousTransactionHash, ownerOnePublicKeyEncoded);

    //         // Hash these two together before signing
    //         byte[] hashedPreviousBuffer = md.digest(pkPreviousBuffer);
    //         md.reset();

    //         // Now sign the transaction with the oldOwner's private key
    //         // This is our "verification" that the oldOwner is signing their coin to the newOwner
    //         sig.update(hashedPreviousBuffer);
    //         byte[] signature = sig.sign();
    //         System.out.println("Signed transaction");
    //         System.out.printf("Transaction signature (Base64): %s\n", Base64.getEncoder().encodeToString(signature));

    //         // Add this signature to the transaction
    //         firstTransaction.oldOwnerSignature = signature;

    //         // Great, now that we have our first transaction, let's try making another one and verifying it!
    //         // Creating another keyPair for ownerTwo
    //         KeyPair ownerTwoKeyPair = keyGen.generateKeyPair();
    //         Transaction secondTransaction = new Transaction();
    //         secondTransaction.newOwnerKey = ownerTwoKeyPair.getPublic();
    //         secondTransaction.amount = 1;
    //         secondTransaction.timestampHash = md.digest(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
    //         md.reset();

    //         // Let's now actually hash and sign it, verifying the signature with our previous owner!

    //         // Create a buffer that contains the newOwner public key and the hash of previous transaction.
    //         byte[] thirdOwnerPublicKey = secondTransaction.newOwnerKey.getEncoded();
    //         byte[] transactionHash = firstTransaction.hash();

    //         // Create the new buffer for signing
    //         pkPreviousBuffer = Util.concatenateBuffers(transactionHash, thirdOwnerPublicKey);
            
    //         // Hash the two together
    //         byte[] secondTransactionPreviousBuffer = md.digest(pkPreviousBuffer);
    //         md.reset();

    //         // Initialize the signature for second transaction :D
    //         Signature secondTransactionSignature = Signature.getInstance("SHA256withRSA");

    //         // Initialize the signature with the private key of the previous owner (ownerOne)
    //         secondTransactionSignature.initSign(ownerOnePrivateKey);
            
    //         // Add the hash of (previousTransaction + third owner public key) as the data for our signature
    //         secondTransactionSignature.update(secondTransactionPreviousBuffer);

    //         // Sign it and put it as data for our transaction
    //         byte[] secondTransactionSignatureBuffer = secondTransactionSignature.sign();
    //         secondTransaction.oldOwnerSignature = secondTransactionSignatureBuffer;

    //         // Verify this signature with ownerOne (this should pass)
    //         secondTransactionSignature.initVerify(ownerOnePublicKey);

    //         // NOTE: sign() and initVerify() resets the signature object, so you MUST update with the buffer before verifying 
    //         secondTransactionSignature.update(secondTransactionPreviousBuffer);
    //         System.out.printf("Owner1 Second Transaction verify (should pass): %s\n", (secondTransactionSignature.verify(secondTransactionSignatureBuffer)) ? "PASS" : "FAIL");
            

    //         // Verify this signature with ownerZero (this should fail)
    //         secondTransactionSignature.initVerify(ownerZeroKeyPair.getPublic());
    //         secondTransactionSignature.update(secondTransactionPreviousBuffer);
    //         System.out.printf("Owner0 Second Transaction verify (should fail): %s\n", (secondTransactionSignature.verify(secondTransactionSignatureBuffer)) ? "PASS" : "FAIL");
    //     } catch(Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // Debug function to give coins to someone, remove this when the network is really working
    public static TransactionOutput mintCoinDebug(Wallet recipient, double amount) {
        System.out.printf("Attempting to mint %.2f coins for user %s\n", amount, recipient.getName());
        TransactionOutput output = new TransactionOutput(amount, recipient.getPublicKey().getEncoded());
        return output;
    }

    private static ArrayList<Block> savedBlocks = new ArrayList<>();

    public static boolean isTransactionOutputSpent(ArrayList<Block> blockchain, TransactionId transactionId, int outputId) {
        for(Block block : blockchain) {
            if(!block.contains(transactionId))
                continue;
            Transaction transaction = block.getTransactionById(transactionId);
            for(TransactionInput input : transaction.getInputs()) {
                // If there exists a transaction already with the output listed, then it is for sure spent
                if(input.previousOutput == outputId)
                    return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPair GOD_KEY_PAIR = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        ArrayList<Block> blocks = new ArrayList<>();

        Wallet bobby = new Wallet("bobby");
        Wallet larry = new Wallet("larry");
        Wallet jimmy = new Wallet("jimmy");

        // Transaction which contains a few shenanigans!
        // Mint a coin for bobby
        Transaction mintTransaction = new Transaction();
        mintTransaction.addOutput(mintCoinDebug(bobby, 1));

        // Now let's DISTRIBUTE some coins to our friends!! :DD
        Transaction funnyTransaction = new Transaction();

        // Our only input to this transaction will be the mint transaction!
        TransactionInput mintTransactionInput = new TransactionInput();
        mintTransactionInput.previousTransaction = mintTransaction.getTransactionId();
        mintTransactionInput.previousOutput = 0;
        mintTransactionInput.signature = new byte[] {0x00, 0x01}; // we dont gaf about SIGNATURES rn
        mintTransactionInput.signatureSize = 2;

        mintTransaction.addInput(mintTransactionInput);
        // now let's send some to our friends!
        funnyTransaction.addOutput(new TransactionOutput(0.5, larry.getPublicKey().getEncoded()));
        funnyTransaction.addOutput(new TransactionOutput(0.2, jimmy.getPublicKey().getEncoded()));

        // We gave some coins to others, but to update our OWN wallet and ensure not a double-spend, collect our change!
        // TODO: implement full UTXO (unspent transaction output) logic
        funnyTransaction.addOutput(new TransactionOutput(0.3, bobby.getPublicKey().getEncoded()));
        // okay now let's add our funny transactions to the block and see what happens!

        // Create a block to put our transactions into!
        Block testBlock = new Block(mintTransaction);
        testBlock.addTransaction(funnyTransaction);

        // add some garbage data because we dont gaf rn
        testBlock.previousBlockHash = new byte[] {0x00, 0x01};
        testBlock.timestampHash = Util.hashBuffer(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
        System.out.println(testBlock);
        savedBlocks.add(testBlock);

        // TODO: Payment verification process
        // 1. have transaction object that we want to get
        // 2. get all blocks headers
        // 3. find the block that has the same timestamp as our transaction
        // 4. calculate merkle proof (need to implement this)
        // https://wiki.bitcoinsv.io/index.php/Simplified_Payment_Verification
        // basically ask the node to verify the transaction by having it take the hash of the transaction
        // and merkle tree from that. if eventually we get to the merkle root, then we know that the payment is valid
        // https://medium.com/crypto-0-nite/merkle-proofs-explained-6dd429623dc5
        // https://ethereum.org/en/developers/tutorials/merkle-proofs-for-offline-data-integrity/

        // Maybe switch to the account paradigm used by ETH?
        // https://ethereum.stackexchange.com/questions/10267/how-ethereum-confirm-the-transaction
        // https://ethereum.org/en/developers/docs/accounts/
        // https://ethereum.org/en/developers/docs/evm/
        // The other option is to refactor the Transactions to use inputs and outputs, however implmenting the EVM
        // May be a more fun idea? not sure yet.

        System.out.println("Did it work? idk bobby should now have 0.5");
        System.out.println("Now checking the nonce for testBlock. Let's see how long this takes");
        
        Proof.hashUntilValid(testBlock);
    }
}