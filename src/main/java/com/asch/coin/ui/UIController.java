package com.asch.coin.ui;

import com.asch.coin.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

public class UIController {
    @FXML
    public ChoiceBox<Wallet> walletChooser;
    @FXML
    public ChoiceBox<Wallet> recipientChooser;

    @FXML
    public Button sendButton;

    @FXML
    public Button mintButton;

    @FXML
    public TextField amountField;

    @FXML
    public ScrollPane blockContainerScroll;

    @FXML
    public HBox innerBlockContainer;

    @FXML
    public Label balanceLabel;

    @FXML
    public Label pubKeyLabel;

    public void initialize() {
        walletChooser.setItems(FXCollections.observableList(Core.wallets));
        recipientChooser.setItems(FXCollections.observableList(Core.wallets));
        walletChooser.converterProperty().set(new WalletStringConverter());
        recipientChooser.converterProperty().set(new WalletStringConverter());
        walletChooser.valueProperty().addListener((observable, oldWallet, curWallet) -> {
            if(curWallet == null)
                return;
            balanceLabel.setText(String.format("%.3f", curWallet.getBalance()));
            pubKeyLabel.setText(Util.bytesToHex(Objects.requireNonNull(Util.hashBuffer(curWallet.getPublicKey().getEncoded()))).substring(0, 20));
        });
        sendButton.onActionProperty().set(new SendCoinHandler(this));
        mintButton.onActionProperty().set(new MintCoinHandler(this));

        updateBlockDisplay();
        }
        private int currentBlockIndex = 0;
    public void updateBlockDisplay() {
        innerBlockContainer.setSpacing(20);

        for(int i=currentBlockIndex;i<Blockchain.getInstance().getBlocks().size();i++) {
            Block givenBlock = Blockchain.getInstance().getBlocks().get(i);

            VBox container = new VBox();
            Label blockHash = new Label();
            blockHash.setText("Block: " + Util.bytesToHex(givenBlock.hash()));
            Label nonce = new Label();

            // Get timestamp of block creation
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = Instant.ofEpochSecond(givenBlock.getTimestamp()).atZone(Calendar.getInstance().getTimeZone().toZoneId()).format(formatter);
            nonce.setText("Nonce: " + givenBlock.nonce + "\nBlock Time: " + formattedTime);
            container.getChildren().add(blockHash);
            container.getChildren().add(nonce);

            ScrollPane transactionContainerPane = new ScrollPane();
            VBox transactionContainer = new VBox();
            for(Transaction t : givenBlock.getTransactions()) {
                transactionContainer.getChildren().add(getTransactionSummary(t));
            }
            transactionContainer.setSpacing(10);
            transactionContainer.autosize();
            transactionContainerPane.setContent(transactionContainer);
            transactionContainerPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            container.getChildren().add(transactionContainerPane);
            innerBlockContainer.getChildren().add(container);
            currentBlockIndex++;
        }
        blockContainerScroll.setContent(innerBlockContainer);
        blockContainerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        blockContainerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @SuppressWarnings("unchecked")
    private VBox getTransactionSummary(Transaction transaction) {
        VBox container = new VBox();
        Label transactionId = new Label();
        transactionId.setText("TXID: " + transaction.getTransactionId().toString()+"\n");
        if(Util.bufferEquality(transaction.getInputs().get(0).getSignature(), "Hello Coin!".getBytes())) {
            transactionId.setText(transactionId.getText() + "(Coinbase Transaction)");
        }
        container.getChildren().add(transactionId);

        TableView<TransactionInput> transactionInputTree = new TableView<>();

        // TransactionInput sub-columns
        TableColumn<TransactionInput, String> prevTransactionIdColumn = new TableColumn<>("PrevTXID");
        prevTransactionIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPreviousTransaction().toString()));

        TableColumn<TransactionInput, String> previousOutputColumn = new TableColumn<>("PrevOutputNum");
        previousOutputColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getPreviousOutput())));

        transactionInputTree.getColumns().addAll(prevTransactionIdColumn, previousOutputColumn);
        transactionInputTree.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transactionInputTree.setMaxHeight(100);

        // Populate transactionInputTree with data
        transactionInputTree.getItems().addAll(transaction.getInputs());

        TableView<TransactionOutput> transactionOutputTree = new TableView<>();

        // TransactionOutput sub-columns
        TableColumn<TransactionOutput, String> destinationPublicKeyColumn = new TableColumn<>("DestPublicKey");
        destinationPublicKeyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Util.bytesToHex(Objects.requireNonNull(Util.hashBuffer(cellData.getValue().getDestinationPublicKey())))));

        TableColumn<TransactionOutput, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Double.toString(cellData.getValue().getAmount())));

        transactionOutputTree.getColumns().addAll(destinationPublicKeyColumn, amountColumn);
        transactionOutputTree.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transactionOutputTree.setMaxHeight(100);

        transactionOutputTree.getItems().addAll(transaction.getOutputs());

        // Add Input/Outputs and Labels to the container
        Label inputLabel = new Label();
        inputLabel.setText("Inputs");
        container.getChildren().add(inputLabel);
        container.getChildren().add(transactionInputTree);

        Label outputLabel = new Label();
        outputLabel.setText("Outputs");
        container.getChildren().add(outputLabel);
        container.getChildren().add(transactionOutputTree);

        return container;
    }

    public void shutdown() {
    }
}