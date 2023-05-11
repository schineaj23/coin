module com.asch.coin {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.asch.coin to javafx.fxml;
    exports com.asch.coin;
}