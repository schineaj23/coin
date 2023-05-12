package com.asch.coin.ui;

import com.asch.coin.*;
import javafx.scene.Node;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class BlockTreeTableView {

    public static Node create(Block block) {
        TreeTableView<Block> treeTableView = new TreeTableView<>();

        // Create main transactions column
        TreeTableColumn<Block, String> transactionsColumn = new TreeTableColumn<>("Transactions");

        // Create subcolumns for the transaction for inputs/outputs
        TreeTableColumn<Block, String> txInputColumn = new TreeTableColumn<>("Inputs");
        // txin subcolumns
        TreeTableColumn<Block, String> prevTransactionIdColumn = new TreeTableColumn<>("PrevTXID");
        TreeTableColumn<Block, String> previousOutputColumn = new TreeTableColumn<>("PrevOutputNum");
        txInputColumn.getColumns().addAll(prevTransactionIdColumn, previousOutputColumn);

        TreeTableColumn<Block, String> txOutputColumn = new TreeTableColumn<>("Outputs");
        //txout subcolumns
        TreeTableColumn<Block, String> amountColumn = new TreeTableColumn<>("Amount");
        TreeTableColumn<Block, String> destinationPublicKeyColumn = new TreeTableColumn<>("DestPublicKey");
        txOutputColumn.getColumns().addAll(amountColumn, destinationPublicKeyColumn);

        // add outputs to transactions
        transactionsColumn.getColumns().addAll(txInputColumn, txOutputColumn);
        treeTableView.getColumns().add(transactionsColumn);

        return null;
    }
}
