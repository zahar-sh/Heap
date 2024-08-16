package com.example.heap.controller;

import com.example.heap.exception.LoadingException;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.InputStream;

public class ControllerLoader {

    private final Class<?> cls;

    public ControllerLoader(Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("class is null");
        }
        this.cls = cls;
    }

    public <T extends Controller> T load(String path) throws LoadingException {
        if (path == null) {
            throw new LoadingException("path is null");
        }
        InputStream resource = cls.getResourceAsStream(path);
        if (resource == null) {
            throw new LoadingException("resource not found");
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.load(resource);
        } catch (IOException e) {
            throw new LoadingException(e);
        }
        return fxmlLoader.getController();
    }
}
