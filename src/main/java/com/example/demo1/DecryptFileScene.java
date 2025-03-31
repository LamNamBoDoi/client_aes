package com.example.demo1;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DecryptFileScene {
    private final Stage stage;
    private final String username;
    private final Runnable onBack;
    private File selectedFile;

    private final TextField fileLabel = new TextField("No file selected");
    private final PasswordField keyField = new PasswordField();
    private final ComboBox<String> keySizeComboBox = new ComboBox<>();

    public DecryptFileScene(Stage stage, String username, Runnable onBack) {
        this.stage = stage;
        this.username = username;
        this.onBack = onBack;

        fileLabel.setEditable(false);
        fileLabel.setMaxWidth(Double.MAX_VALUE);
        keyField.setPromptText("Enter decryption key");
        keyField.setMaxWidth(Double.MAX_VALUE);
        keySizeComboBox.getItems().addAll("128 bits", "192 bits", "256 bits");
        keySizeComboBox.setValue("128 bits");
        keySizeComboBox.setMaxWidth(Double.MAX_VALUE);
    }

    public Scene createScene() {
        // Title
        Label titleLabel = new Label("FILE DECRYPTION");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3a5169;");

        // Form Card
        VBox formCard = new VBox(10);
        formCard.setMaxWidth(500);
        formCard.setMaxHeight(300);
        formCard.setPadding(new Insets(10));

        // Styling for form elements
        fileLabel.setStyle("-fx-padding: 8;");
        keyField.setStyle("-fx-padding: 8;");
        keySizeComboBox.setStyle("-fx-padding: 8;");

        // Buttons
        Button chooseFileButton = createStyledButton("SELECT", "/file.png", "#2196F3");
        chooseFileButton.setOnAction(e -> openFileChooser());

        Button decryptButton = createStyledButton("DECRYPT FILE", "/unlocked.png", "#4CAF50");
        decryptButton.setStyle(decryptButton.getStyle() + "-fx-font-weight: bold;");
        decryptButton.setOnAction(e -> decryptFile());

        Button backButton = createStyledButton("BACK", "/back.png", "#616161");
        backButton.setOnAction(e -> onBack.run());

        // Tạo bố cục lưới cho biểu mẫu
        GridPane formGrid = new GridPane();
        formGrid.setVgap(10); // Khoảng cách giữa các hàng
        formGrid.setHgap(10); // Khoảng cách giữa các cột
        formGrid.setPadding(new Insets(10));
        // Cấu hình cột để tránh chữ bị cắt
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100); // Độ rộng tối thiểu của cột chứa nhãn
        col1.setPrefWidth(120); // Độ rộng ưu tiên để tránh cắt chữ
        col1.setHgrow(Priority.NEVER); // Không cho cột này mở rộng

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS); // Cho phép cột mở rộng để chứa nội dung

        formGrid.getColumnConstraints().addAll(col1, col2);

        // Add form elements with styled labels
        addFormRow(formGrid, 0, "ENCRYPTED FILE:", fileLabel, chooseFileButton);
        addFormRow(formGrid, 1, "DECRYPTION KEY:", keyField);
        addFormRow(formGrid, 2, "KEY SIZE:", keySizeComboBox);

        // Button container
        HBox buttonBox = new HBox(10, backButton, decryptButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        formCard.getChildren().addAll(formGrid, new Separator(), buttonBox);

        // Main layout
        VBox mainLayout = new VBox(10, titleLabel , formCard);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(10));

        mainLayout.setPrefSize(450, 320);

        return new Scene(mainLayout);
    }

    private void addFormRow(GridPane grid, int row, String labelText, Control control) {
        addFormRow(grid, row, labelText, control, null);
    }

    private void addFormRow(GridPane grid, int row, String labelText, Control control, Node extraNode) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        GridPane.setHalignment(label, javafx.geometry.HPos.LEFT);

        grid.add(label, 0, row);

        if (extraNode != null) {
            HBox container = new HBox(10, control, extraNode);
            container.setAlignment(Pos.CENTER_LEFT);
            grid.add(container, 1, row);
            GridPane.setHgrow(container, Priority.ALWAYS);
        } else {
            grid.add(control, 1, row);
        }

        GridPane.setHgrow(control, Priority.ALWAYS);
    }

    private Button createStyledButton(String text, String iconPath, String color) {
        Button button = new Button(text, loadImage(iconPath, 16, 16));
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 4; " +
                "-fx-font-weight: bold; "+
                "-fx-padding: 8 15 8 15; " +
                "-fx-cursor: hand;");
        return button;
    }

    private ImageView loadImage(String path, double width, double height) {
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            return imageView;
        } catch (Exception e) {
            return new ImageView();
        }
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        File receivedDir = new File("users/" + username + "/Received");
        if (!receivedDir.exists()) receivedDir.mkdirs();
        fileChooser.setInitialDirectory(receivedDir);

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getName());
            this.selectedFile = selectedFile;
        }
    }

    private void decryptFile() {
        if (selectedFile == null) {
            showErrorAlert("Chưa chọn tệp", "Vui lòng chọn tệp đã mã hóa trước.");
            return;
        }

        String key = keyField.getText().trim();
        if (key.isEmpty()) {
            showErrorAlert("Thiếu khóa", "Vui lòng nhập khóa giải mã.");
            return;
        }

        if (keySizeComboBox.getValue() == null) {
            showErrorAlert("Chưa chọn kích thước khóa", "Vui lòng chọn kích thước khóa được sử dụng để mã hóa.");
            return;
        }

        File decryptDir = new File("users/" + username + "/Decrypted");
        if (!decryptDir.exists()) decryptDir.mkdirs();

        // Loại bỏ phần mở rộng .enc hoặc .aes nếu có
        String originalName = selectedFile.getName();
        if (originalName.endsWith(".enc") || originalName.endsWith(".aes")) {
            originalName = originalName.substring(0, originalName.lastIndexOf('.'));
        }

        File decryptedFile = new File(decryptDir, originalName);

        try {
            // Đọc IV từ đầu file (16 byte)
            byte[] iv = new byte[16];
            FileInputStream fis = new FileInputStream(selectedFile);
            if (fis.read(iv) != iv.length) {
                showErrorAlert("Lỗi tệp", "Không thể đọc IV từ tệp.");
                fis.close();
                return;
            }

            // Đọc phần còn lại là dữ liệu mã hóa
            byte[] encryptedData = fis.readAllBytes();
            fis.close();

            // Chuyển dữ liệu mã hóa sang chuỗi hex hoặc base64
            String encryptedHex = bytesToHex(encryptedData);
            String ivHex = bytesToHex(iv);

            // Giải mã dữ liệu
            CBC cbc = new CBC();
            String decryptedHex = cbc.decrypt(encryptedHex, ivHex, key);

            // Chuyển từ hex về byte
            byte[] decryptedData = hexToBytes(decryptedHex);

            // Ghi dữ liệu đã giải mã ra tệp mới
            FileOutputStream fos = new FileOutputStream(decryptedFile);
            fos.write(decryptedData);
            fos.close();

            showSuccessAlert("Giải mã thành công", "Tệp đã được giải mã và lưu tại:\n" + decryptedFile.getAbsolutePath());

            // Mở tệp sau khi giải mã thành công
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(decryptedFile);
            }
        } catch (Exception e) {
            showErrorAlert("Giải mã thất bại", "Lỗi: " + e.getMessage());
        }
    }

    // Hàm chuyển đổi byte[] sang hex string
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // Hàm chuyển đổi hex string sang byte[]
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }


    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccessAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}