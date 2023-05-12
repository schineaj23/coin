package com.asch.coin.ui;


import com.asch.coin.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class MintCoinHandler implements EventHandler<ActionEvent> {
    private final UIController uiControllerInstance;

    public MintCoinHandler(UIController uiController) {
        this.uiControllerInstance = uiController;
    }

    @Override
    public void handle(ActionEvent event) {
        if(uiControllerInstance.recipientChooser.getValue() == null) {
            System.out.println("sendAction: Recipient has a null value. Returning");
            return;
        }

        Wallet recipient = uiControllerInstance.recipientChooser.getValue();

        if(uiControllerInstance.amountField.getCharacters().isEmpty()) {
            System.out.println("sendAction: No amount specified. Returning");
            return;
        }

        // Get the amount to send and create the transaction
        double amount = Double.parseDouble(uiControllerInstance.amountField.getCharacters().toString());

        // This has to be in a try/catch send() may throw InsufficientFundsException
        try {
            Transaction sendTransaction = Core.createCoinbaseTransaction(recipient, amount);
            System.out.println("sendAction: Transaction created: " + sendTransaction);

            // For now going to have this transaction immediately get sent to the block, because why not.
            Block transactionBlock = new Block(sendTransaction);

            // Add this block to the blockchain
            Proof.hashUntilValid(transactionBlock);
            Blockchain.getInstance().addBlock(transactionBlock);
            System.out.println("sendAction: added block to chain");

            uiControllerInstance.updateBlockDisplay();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        recipient.rebuildWallet();
        System.out.printf("NEW Recipient Balance: %.2f\n", recipient.getBalance());
    }
}
