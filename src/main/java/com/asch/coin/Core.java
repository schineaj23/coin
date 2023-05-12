package com.asch.coin;

import com.asch.coin.ui.UIController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;
import java.util.Objects;

public class Core extends Application {
    private static final Wallet bobby = new Wallet("bobby");
    private static final Wallet larry = new Wallet("larry");;
    private static final Wallet jimmy = new Wallet("jimmy");;
    public static ArrayList<Wallet> wallets = new ArrayList<>();

    public static Transaction createCoinbaseTransaction(Wallet recipient, double amount) {
        Transaction transaction = new Transaction();
        TransactionInput input = new TransactionInput(new TransactionId(), 0xFFFFFFFF,
                new String("Hello Coin!").getBytes());
        TransactionOutput output = new TransactionOutput(amount, recipient.getPublicKey().getEncoded());

        System.out.printf("createCoinbaseTransaction(): Minting %.2f coins for user %s\n", amount, recipient.getName());
        transaction.addInput(input);
        transaction.addOutput(output);

        // This is a new transaction, therefore by definition will generate a UTXO
        Blockchain.getInstance().addUnspentTransactionOutput(new UnspentTransactionOutput(transaction.getTransactionId(), 0, output));
        return transaction;
    }

    private static void test() {
        // Transaction which contains a few shenanigans!
        // Mint a coin for bobby
        Transaction coinbaseTransaction = createCoinbaseTransaction(bobby, 2);
        System.out.println(coinbaseTransaction);

        // Create a block to put our transactions into!
        Block testBlock = new Block(coinbaseTransaction);
        // testBlock.addTransaction(funnyTransaction);

        // add some garbage data because we dont gaf rn
        testBlock.previousBlockHash = new byte[] { 0x00, 0x01 };
        testBlock.timestampHash = Util.hashBuffer(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());

        System.out.println("Did it work? idk bobby should now have 0.5");
        System.out.println("Now checking the nonce for testBlock. Let's see how long this takes");

        Proof.hashUntilValid(testBlock);
        Blockchain.getInstance().addBlock(testBlock);
        System.out.println(testBlock);

        System.out.println("So that worked. Now I'm going to try doing it based on wallet");

        Transaction bobbyToJimmy = bobby.send(jimmy, 0.8);

        // We cannot have the coinbase transaction and the wallet transaction on the
        // same block
        // Because we would have no knowledge of the first transaction until all the
        // others are collected
        // Thus resulting in a double spend.
        Block coolBlock = new Block(bobbyToJimmy);
        coolBlock.addTransaction(jimmy.send(larry, 0.1));
        coolBlock.addTransaction(larry.send(bobby, 0.05));
        coolBlock.previousBlockHash = Blockchain.getInstance().getMostRecentBlock().hash();

        Proof.hashUntilValid(coolBlock);
        Blockchain.getInstance().addBlock(coolBlock);

        jimmy.rebuildWallet();
        // Jimmy is basically an offline wallet, refresh jimmy with everything to see
        // his balance
        // Ensure that bobby has the correct balance as well
        bobby.rebuildWallet();
        larry.rebuildWallet();
    }

    public Wallet[] getWallets() {
        return new Wallet[] {bobby, larry, jimmy};
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException {
        // Create a new instance so that it doesn't get mad at us.
        wallets.add(bobby);
        wallets.add(jimmy);
        wallets.add(larry);

        test();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("layout.fxml")));
        Parent root = loader.load();
        primaryStage.setTitle("DrewCoin");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        UIController controller = loader.getController();

        primaryStage.setOnHidden(e -> {
            controller.shutdown();
            Platform.exit();
        });
        primaryStage.show();
    }
}