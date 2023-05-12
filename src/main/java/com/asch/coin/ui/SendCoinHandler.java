package com.asch.coin.ui;


import com.asch.coin.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class SendCoinHandler implements EventHandler<ActionEvent> {
    private final UIController uiControllerInstance;

    public SendCoinHandler(UIController uiController) {
        this.uiControllerInstance = uiController;
    }

    @Override
    public void handle(ActionEvent event) {
        if(uiControllerInstance.recipientChooser.getValue() == null || uiControllerInstance.walletChooser.getValue() == null) {
            System.out.println("sendAction: One of the choosers has a null value. Returning");
            return;
        }

        Wallet sender = uiControllerInstance.walletChooser.getValue();
        Wallet recipient = uiControllerInstance.recipientChooser.getValue();

        if(sender.getName().equals(recipient.getName())) {
            System.out.println("sendAction: Cannot send to self!");
            return;
        }

        if(uiControllerInstance.amountField.getCharacters().isEmpty()) {
            System.out.println("sendAction: No amount specified. Returning");
            return;
        }

        // Get the amount to send and create the transaction
        double amount = Double.parseDouble(uiControllerInstance.amountField.getCharacters().toString());

        // This has to be in a try/catch send() may throw InsufficientFundsException
        try {
            Transaction sendTransaction = sender.send(recipient, amount);
            System.out.println("sendAction: Transaction created: " + sendTransaction);

            // For now going to have this transaction immediately get sent to the block, because why not.
            Block transactionBlock = new Block(sendTransaction);

            // Add this block to the blockchain
            Proof.hashUntilValid(transactionBlock);
            Blockchain.getInstance().addBlock(transactionBlock);
            System.out.println("sendAction: added block to chain");
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        sender.rebuildWallet();
        recipient.rebuildWallet();
        System.out.printf("NEW Sender Balance: %.2f, NEW Recipient Balance: %.2f\n", sender.getBalance(), recipient.getBalance());
        uiControllerInstance.updateBlockDisplay();
    }
}
