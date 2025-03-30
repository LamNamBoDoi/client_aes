package com.example.demo1;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SendFileScene {
    private final Stage stage;
    private final String username;
    private final String serverIp;
    private final int serverPort;
    private final Runnable onBack;
    private File selectedFile;
    private final Label fileLabel;
    private final PasswordField keyField;
    private final ComboBox<String> recipientComboBox;
    private final ComboBox<String> keySizeComboBox;
    private final ProgressIndicator progressIndicator;

    public SendFileScene(Stage stage, String username, String serverIp, int serverPort, Runnable onBack) {
        this.stage = stage;
        this.username = username;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.onBack = onBack;

        // Initialize UI components with improved styling
        fileLabel = new Label("No file selected");
        fileLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        keyField = new PasswordField();
        keyField.setPromptText("Enter encryption key");
        keyField.setStyle("-fx-font-size: 14px; -fx-prompt-text-fill: #aaa;");

        recipientComboBox = new ComboBox<>();
        recipientComboBox.setPromptText("Select recipient");
        recipientComboBox.setStyle("-fx-font-size: 12px; -fx-pref-width: 150;");

        keySizeComboBox = new ComboBox<>();
        keySizeComboBox.getItems().addAll("128-bit", "192-bit", "256-bit");
        keySizeComboBox.setValue("128-bit");
        keySizeComboBox.setStyle("-fx-font-size: 12px; -fx-pref-width: 150;");

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
    }

    public Scene createScene() {
        requestClientList(); // Yêu cầu danh sách các client hiện đang kết nối

        // Tạo tiêu đề cho giao diện gửi file
        Label titleLabel = new Label("SEND ENCRYPTED FILE"); // Nhãn tiêu đề
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3a5169;"); // Định dạng tiêu đề

        // Tạo các nút bấm với icon tương ứng
        Button backButton = createActionButton("Back", "/back.png", "#6c757d"); // Nút quay lại
        Button sendButton = createActionButton("Send", "/send.png", "#28a745"); // Nút gửi file
        Button chooseFileButton = createActionButton("Select File", "/file.png", "#17a2b8"); // Nút chọn file
        Button refreshButton = createActionButton("Refresh", "/refresh.png", "#6f42c1"); // Nút làm mới danh sách người nhận

        // Tạo lưới bố cục cho form nhập liệu
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10); // Khoảng cách ngang giữa các phần tử
        formGrid.setVgap(10); // Khoảng cách dọc giữa các phần tử
        formGrid.setPadding(new Insets(15)); // Khoảng cách lề xung quanh

        // Thêm các phần tử vào form
        formGrid.add(new Label("File:"), 0, 0); // Nhãn "File"
        formGrid.add(fileLabel, 1, 0); // Hiển thị tên file đã chọn
        formGrid.add(chooseFileButton, 2, 0); // Nút chọn file

        formGrid.add(new Label("Key:"), 0, 1); // Nhãn "Key"
        formGrid.add(keyField, 1, 1, 2, 1); // Trường nhập khóa mã hóa

        formGrid.add(new Label("Key Size:"), 0, 2); // Nhãn "Key Size"
        formGrid.add(keySizeComboBox, 1, 2); // Chọn kích thước khóa mã hóa

        formGrid.add(new Label("Recipient:"), 0, 3); // Nhãn "Recipient"
        formGrid.add(recipientComboBox, 1, 3); // Chọn người nhận file
        formGrid.add(refreshButton, 2, 3); // Nút làm mới danh sách người nhận

        // Hộp chứa các nút bấm (Back & Send)
        HBox buttonBox = new HBox(12, backButton, sendButton); // Đặt nút Back trước Send
        buttonBox.setAlignment(Pos.CENTER); // Căn giữa các nút

        // Bố cục chính của giao diện
        VBox mainLayout = new VBox(16, titleLabel, formGrid, progressIndicator, buttonBox);
        mainLayout.setAlignment(Pos.CENTER); // Căn giữa nội dung
        mainLayout.setPadding(new Insets(20)); // Khoảng cách lề

        // Tạo scene và áp dụng stylesheet
        Scene scene = new Scene(mainLayout, 450, 320);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/sendfile.css")).toExternalForm());

        return scene; // Trả về scene đã tạo
    }

    private Button createActionButton(String text, String iconPath, String color) {
        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        icon.setFitWidth(16);
        icon.setFitHeight(16);

        Button button = new Button(text, icon);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(10);
        button.setStyle("-fx-font-size: 14px; -fx-padding: 8 15 8 15; -fx-background-radius: 5;");
        button.setStyle("-fx-base: " + color + ";");
        button.setMinWidth(120);

        // Hiệu ứng hover
        button.setOnMouseEntered(e -> button.setStyle("-fx-base: " + color + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.5, 0, 1);"));
        button.setOnMouseExited(e -> button.setStyle("-fx-base: " + color + "; -fx-effect: null;"));

        return button;
    }

    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            fileLabel.setText(file.getName());
            fileLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    public void requestClientList() {
        new Thread(() -> {
            try (Socket socket = new Socket(serverIp, serverPort)) {
                DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInput = new DataInputStream(socket.getInputStream());

                // Gửi yêu cầu danh sách client
                dataOutput.writeUTF("LIST_CLIENTS");
                dataOutput.flush();

                // Nhận danh sách từ server
                String response = dataInput.readUTF();
                List<String> clients = Arrays.asList(response.split(","));
                clients.removeIf(String::isBlank); // Loại bỏ các mục rỗng
                clients.remove(username); // Loại bỏ chính mình

                Platform.runLater(() -> {
                    if (clients.isEmpty()) {
                        showAlert("Lỗi", "Không có client online");
                    } else {
                        // Cập nhật ComboBox với danh sách các client còn lại
                        recipientComboBox.getItems().setAll(clients);
                        recipientComboBox.setDisable(false); // Mở khóa ComboBox khi có client
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể lấy danh sách client: " + e.getMessage());
                });
            }
        }).start();
    }

    private void sendFile() {
        if (selectedFile == null) {
            showAlert("Warning", "Please select a file first!");
            return;
        }

        String key = keyField.getText().trim();
        String receiver = recipientComboBox.getValue();
        if (receiver == null || receiver.isEmpty()) {
            showAlert("Warning", "Please select a recipient!");
            return;
        }
        if (key.isEmpty()) {
            showAlert("Warning", "Please enter the encryption key!");
            return;
        }

        progressIndicator.setVisible(true);
        new Thread(() -> {
            try (Socket socket = new Socket(serverIp, serverPort);
                 DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
                 FileInputStream fileInput = new FileInputStream(selectedFile)) {

                dataOutput.writeUTF("START_FILE");
                dataOutput.writeUTF(receiver);
                dataOutput.writeUTF(selectedFile.getName());
                dataOutput.writeLong(selectedFile.length());
                dataOutput.flush();

                byte[] buffer = new byte[65536];
                int bytesRead;
                while ((bytesRead = fileInput.read(buffer)) != -1) {
                    dataOutput.write(buffer, 0, bytesRead);
                }
                dataOutput.flush();

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert("Success", "File sent successfully to " + receiver + "!");
                    resetForm();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert("Error", "Failed to send file: " + e.getMessage());
                });
            }
        }).start();
    }

    private void resetForm() {
        selectedFile = null;
        fileLabel.setText("No file selected");
        fileLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        keyField.clear();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/alerts.css")).toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");

            alert.showAndWait();
        });
    }
}