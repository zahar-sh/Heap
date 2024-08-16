module com.example.heap {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;

    opens com.example.heap to javafx.fxml;
    exports com.example.heap;
    exports com.example.heap.controller;
    exports com.example.heap.exception;
    opens com.example.heap.controller to javafx.fxml;
    opens com.example.heap.exception to javafx.fxml;
}