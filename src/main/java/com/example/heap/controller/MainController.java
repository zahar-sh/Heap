package com.example.heap.controller;

import com.example.heap.core.ArrayHeapQueue;
import com.example.heap.core.HeapQueue;
import com.example.heap.exception.LoadingException;
import com.example.heap.exception.ValidationException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.synedra.validatorfx.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class MainController implements Controller {

    private static final String FXML_PATH = "main-view.fxml";

    private static final String ADD_KEY = "add";
    private static final String CONTAINS_KEY = "contains";
    private static final String GET_KEY = "get";
    private static final String REMOVE_KEY = "remove";
    private static final String NODE_WIDTH_KEY = "element-width";
    private static final String NODE_HEIGHT_KEY = "element-height";

    private static final Pattern OPTIONAL_NUMBER = Pattern.compile("[-+]?\\d*");

    public static MainController load(ControllerLoader loader) throws LoadingException {
        return loader.load(FXML_PATH);
    }

    @FXML
    private Pane root;

    @FXML
    private Canvas canvas;

    @FXML
    private TextField addTextField;

    @FXML
    private TextField containsTextField;

    @FXML
    private TextField getTextField;

    @FXML
    private TextField removeTextField;

    @FXML
    private Button pollButton;

    @FXML
    private Button formatButton;

    @FXML
    private Button clearButton;

    @FXML
    private TextField elementWidthTextField;

    @FXML
    private TextField elementHeightTextField;

    @FXML
    private ColorPicker elementColorColorPicker;

    @FXML
    private ColorPicker selectedColorPicker;

    @FXML
    private ColorPicker linksColorPicker;

    private HeapQueue<Integer> heapQueue;

    private Map<Integer, Point> pointMap;

    private Integer selectedIndex;

    private Size elementSize;

    private Font font;

    private double lineWidth;

    //  difference between selected element pos and mouse pressed
    private Point delta;

    private Point selectedPos;

    @FXML
    private void initialize() {
        heapQueue = new ArrayHeapQueue<>(Integer::compareTo);
        pointMap = new HashMap<>();
        heapQueue.offer(5);
        heapQueue.offer(3);
        heapQueue.offer(8);
        heapQueue.offer(1);
        heapQueue.offer(9);
        heapQueue.offer(6);
        heapQueue.offer(2);
        heapQueue.offer(4);
        heapQueue.offer(7);

        elementSize = new Size(50, 50);

        font = Font.font("Consolas", FontWeight.BOLD, 14);
        lineWidth = 1.5;

        delta = new Point();

        elementWidthTextField.setText(Integer.toString(elementSize.getWidth()));
        elementHeightTextField.setText(Integer.toString(elementSize.getHeight()));
        elementColorColorPicker.setValue(Color.CHOCOLATE);
        selectedColorPicker.setValue(Color.YELLOW);
        linksColorPicker.setValue(Color.ORANGE);

        Validator validator = new Validator();
        validateDigits(validator, ADD_KEY, addTextField);
        validateDigits(validator, CONTAINS_KEY, containsTextField);
        validateDigits(validator, GET_KEY, getTextField);
        validateDigits(validator, REMOVE_KEY, removeTextField);
        validateDigits(validator, NODE_WIDTH_KEY, elementWidthTextField);
        validateDigits(validator, NODE_HEIGHT_KEY, elementHeightTextField);

        addTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::add))));
        containsTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::contains))));
        getTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::get))));
        removeTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::remove))));
        pollButton.setOnAction(onAction(runAsync(this::poll)));
        formatButton.setOnAction(onAction(runAsync(() -> {
            format();
            repaint();
        })));
        clearButton.setOnAction(onAction(runAsync(this::clear)));

        elementWidthTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::setWidth))));
        elementHeightTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::setHeight))));
        elementColorColorPicker.setOnAction(onAction(runAsync(this::repaint)));
        selectedColorPicker.setOnAction(onAction(runAsync(this::repaint)));
        linksColorPicker.setOnAction(onAction(runAsync(this::repaint)));

        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);

        format();
        repaint();
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    private void add() throws ValidationException {
        Integer value = getInt(addTextField);
        if (!pointMap.containsKey(value) && heapQueue.offer(value)) {
            pointMap.put(value, centerPoint());
            selectedIndex = heapQueue.indexOf(value);
            format();
            repaint();
        }
    }

    private void contains() throws ValidationException {
        int value;
        try {
            value = getInt(containsTextField);
        } catch (ValidationException e) {
            selectedIndex = null;
            repaint();
            throw e;
        }
        selectedIndex = heapQueue.indexOf(value);
        if (selectedIndex == -1) {
            selectedIndex = null;
        }
        repaint();
    }

    private void get() throws ValidationException {
        int index = getInt(removeTextField);
        selectedIndex = index >= 0 && index < heapQueue.size()
                ? index
                : null;
        repaint();
    }

    private void remove() throws ValidationException {
        int index = getInt(removeTextField);
        if (index >= 0 && index < heapQueue.size()) {
            Integer removed = heapQueue.remove(index);
            pointMap.remove(removed);
            if (selectedIndex != null && selectedIndex == index) {
                selectedIndex = null;
            }
            format();
            repaint();
        }
    }

    private void poll() {
        if (!heapQueue.isEmpty()) {
            Integer element = heapQueue.poll();
            pointMap.remove(element);
            if (selectedIndex != null && selectedIndex == heapQueue.size()) {
                selectedIndex = null;
            }
            format();
            repaint();
        }
    }

    private void setWidth() throws ValidationException {
        int width = getInt(elementWidthTextField);
        elementSize.setWidth(width);
        repaint();
    }

    private void setHeight() throws ValidationException {
        int value = getInt(elementHeightTextField);
        elementSize.setHeight(value);
        repaint();
    }

    private void clear() {
        heapQueue.clear();
        pointMap.clear();
        selectedIndex = null;
        repaint();
    }

    private void format() {
        int treeOffsetY = (int) (elementSize.getHeight() * 2.0);
        int spaceY = (int) (elementSize.getHeight() * 1.5);
        int screenWidth = (int) (canvas.getWidth() - elementSize.getWidth());
        levels:
        for (int level = 0, index = 0; ; level++) {
            int posY = level * spaceY;
            int levelSize = (int) Math.round(Math.pow(2, level));
            double posXFactor = 1 + levelSize;
            for (int levelIndex = 0; levelIndex < levelSize; levelIndex++) {
                if (index == heapQueue.size()) {
                    break levels;
                }
                Integer element = heapQueue.get(index++);
                Point pos = pointMap.computeIfAbsent(element, v -> new Point());
                int posX = (int) ((levelIndex + 1) * (screenWidth / posXFactor));
                pos.setX(posX);
                pos.setY(posY);
                clampBounds(pos);
            }
        }
        pointMap.values().forEach(point -> point.setY(point.getY() + treeOffsetY));
    }

    private int getInt(TextField textField) throws ValidationException {
        int value;
        try {
            value = Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            throw new ValidationException(e);
        }
        return value;
    }

    private void validateDigits(Validator validator, String key, TextField removeTextField) {
        validator.createCheck()
                .dependsOn(key, removeTextField.textProperty())
                .withMethod(context -> {
                    String value = context.get(key);
                    if (!OPTIONAL_NUMBER.matcher(value).matches()) {
                        context.error("Should be a number");
                    }
                })
                .decorates(removeTextField)
                .immediate();
    }

    private EventHandler<ActionEvent> onAction(Runnable action) {
        return keyEvent -> {
            action.run();
        };
    }

    private EventHandler<KeyEvent> onEnterPressed(Runnable action) {
        return keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                action.run();
            }
        };
    }

    private Runnable suppressValidationExceptions(ValidationRunnable action) {
        return () -> {
            try {
                action.run();
            } catch (ValidationException ignored) {
            }
        };
    }

    private Runnable runAsync(Runnable action) {
        return () -> {
            Platform.runLater(action);
        };
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        Integer oldSelectedIndex = selectedIndex;
        if (mouseEvent.isSecondaryButtonDown()) {
            selectedIndex = null;
        }

        int elementWidth = elementSize.getWidth();
        int elementHeight = elementSize.getHeight();
        int mouseX = (int) mouseEvent.getX();
        int mouseY = (int) mouseEvent.getY();

        Point arrayPos = getArrayPos();
        for (int index = 0; index < heapQueue.size(); index++) {
            int x = arrayPos.getX() + index * elementWidth;
            int y = arrayPos.getY();
            if (Geometry.containsPoint(x, y, x + elementWidth, y + elementHeight, mouseX, mouseY)) {
                selectedIndex = index;
                break;
            }
        }

        for (Map.Entry<Integer, Point> entry : pointMap.entrySet()) {
            Point pos = entry.getValue();
            int posX = pos.getX();
            int posY = pos.getY();
            if (Geometry.containsPoint(posX, posY, posX + elementWidth, posY + elementHeight, mouseX, mouseY)) {
                selectedPos = pos;
                delta.setX(posX - mouseX);
                delta.setY(posY - mouseY);
                if (mouseEvent.isSecondaryButtonDown()) {
                    Integer element = entry.getKey();
                    selectedIndex = heapQueue.indexOf(element);
                }
                break;
            }
        }

        if (!Objects.equals(selectedIndex, oldSelectedIndex)) {
            repaint();
        }
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if (selectedPos != null) {
            int mouseX = (int) mouseEvent.getX();
            int mouseY = (int) mouseEvent.getY();
            selectedPos.setX(delta.getX() + mouseX);
            selectedPos.setY(delta.getY() + mouseY);
            clampBounds(selectedPos);
            repaint();
        }
    }

    private void onMouseReleased(MouseEvent mouseEvent) {
        selectedPos = null;
    }

    private Point centerPoint() {
        int elementWidth = elementSize.getWidth();
        int elementHeight = elementSize.getHeight();
        return new Point((int) (canvas.getWidth() - elementWidth) / 2,
                (int) (canvas.getHeight() - elementHeight) / 2);
    }

    private Point getArrayPos() {
        return new Point(elementSize.getWidth() / 2, elementSize.getHeight() / 2);
    }

    private boolean isLink(int index, int linkIndex) {
        return (linkIndex != 0 && index == HeapQueue.parent(linkIndex))
                || index == HeapQueue.left(linkIndex)
                || index == HeapQueue.right(linkIndex);
    }

    private void clampBounds(Point point) {
        Geometry.clamp(point,
                0, (int) (canvas.getWidth() - elementSize.getWidth()),
                0, (int) (canvas.getHeight() - elementSize.getHeight()));
    }

    private void repaint() {
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        context.setLineWidth(lineWidth);
        context.setFont(font);
        context.setStroke(Color.BLACK);

        Color elementColor = elementColorColorPicker.getValue();
        Color selectedColor = selectedColorPicker.getValue();
        Color linksColor = linksColorPicker.getValue();

        int elementWidth = elementSize.getWidth();
        int elementHeight = elementSize.getHeight();

        double elementOffsetX = elementWidth / 2.3;
        double elementOffsetY = elementHeight / 2.0;

        double indexOffsetX = elementOffsetX / 3.0;
        double indexOffsetY = elementOffsetY / 2.0;

        Point arrayPos = getArrayPos();

        int size = heapQueue.size();
        for (int index = 0; index < size; index++) {
            Integer element = heapQueue.get(index);
            Color color = elementColor;
            if (selectedIndex != null) {
                if (index == selectedIndex) {
                    color = selectedColor;
                } else if (isLink(index, selectedIndex)) {
                    color = linksColor;
                }
            }
            int x = arrayPos.getX() + index * elementWidth;
            int y = arrayPos.getY();

            context.setFill(color);
            context.fillRect(x, y, elementWidth, elementHeight);

            context.setFill(Color.BLACK);
            context.strokeRect(x, y, elementWidth, elementHeight);

            context.fillText(Integer.toString(index), x + (elementOffsetX / 3.0), y + (elementOffsetY / 2.0));
            context.fillText(Integer.toString(element), x + elementOffsetX, y + elementOffsetY);
        }

        int centerX = elementWidth / 2;
        int centerY = elementHeight / 2;
        for (int index = 0, mid = (size >>> 1); index < mid; index++) {
            Integer element = heapQueue.get(index);
            Point fromPos = pointMap.get(element);
            int x1 = fromPos.getX() + centerX;
            int y1 = fromPos.getY() + centerY;
            int leftIndex = HeapQueue.left(index);
            if (leftIndex < size) {
                Integer left = heapQueue.get(leftIndex);
                Point toPos = pointMap.get(left);
                int x2 = toPos.getX() + centerX;
                int y2 = toPos.getY() + centerY;
                context.strokeLine(x1, y1, x2, y2);
            }
            int rightIndex = HeapQueue.right(index);
            if (rightIndex < size) {
                Integer right = heapQueue.get(rightIndex);
                Point toPos = pointMap.get(right);
                int x2 = toPos.getX() + centerX;
                int y2 = toPos.getY() + centerY;
                context.strokeLine(x1, y1, x2, y2);
            }
        }

        for (int index = 0; index < size; index++) {
            Integer element = heapQueue.get(index);
            Point elementPos = pointMap.get(element);
            int x = elementPos.getX();
            int y = elementPos.getY();
            Color color = elementColor;
            if (selectedIndex != null) {
                if (index == selectedIndex) {
                    color = selectedColor;
                } else if (isLink(index, selectedIndex)) {
                    color = linksColor;
                }
            }
            context.setFill(color);
            context.fillRect(x, y, elementWidth, elementHeight);

            context.setFill(Color.BLACK);
            context.strokeRect(x, y, elementWidth, elementHeight);
            context.fillText(Integer.toString(index), x + indexOffsetX, y + indexOffsetY);
            context.fillText(Integer.toString(element), x + elementOffsetX, y + elementOffsetY);
        }
    }
}