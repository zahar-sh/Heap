package com.example.heap;

import com.example.heap.controller.ControllerLoader;
import com.example.heap.controller.MainController;
import com.example.heap.exception.LoadingException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HeapApplication extends Application {

    private static final ControllerLoader CONTROLLER_LOADER = new ControllerLoader(HeapApplication.class);

    public static ControllerLoader getControllerLoader() {
        return CONTROLLER_LOADER;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws LoadingException {
        MainController controller = MainController.load(getControllerLoader());
        Scene scene = new Scene(controller.getRoot());
        stage.setTitle("Heap queue viewer");
        stage.setScene(scene);
        stage.show();
    }
}