module com.asch.coin {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.asch.coin to javafx.fxml;
    exports com.asch.coin;
    exports com.asch.coin.ui;
    opens com.asch.coin.ui to javafx.fxml;
}