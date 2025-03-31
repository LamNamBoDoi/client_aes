package com.example.demo1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    private final TextField keyField;
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

        keyField = new TextField();
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
        Button backButton = createActionButton("Back", "/back.png", "#6c757d",e -> onBack.run()); // Nút quay lại
        Button sendButton = createActionButton("Send", "/send.png", "#28a745", e -> handleSendFile()); // Nút gửi file
        Button chooseFileButton = createActionButton("Select File", "/file.png", "#17a2b8", e -> {
            FileChooser fileChooser = new FileChooser();
            selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                fileLabel.setText(selectedFile.getName());
            }
        }); // Nút chọn file
        Button refreshButton = createActionButton("Refresh", "/refresh.png", "#6f42c1",e -> requestClientList()); // Nút làm mới danh sách người nhận

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

    private Button createActionButton(String text, String iconPath, String color, EventHandler<ActionEvent> action) {
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

        // Gán action khi nhấn
        button.setOnAction(action);

        return button;
    }

    public void requestClientList() {
        new Thread(() -> {
            try (Socket socket = new Socket(serverIp, serverPort)) {//Tạo một Socket kết nối tới server:
                DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInput = new DataInputStream(socket.getInputStream());

                // Gửi yêu cầu danh sách client
                dataOutput.writeUTF("LIST_CLIENTS");
                dataOutput.flush();

                // Nhận danh sách từ server
                String response = dataInput.readUTF();
                System.out.println("Received client list: " + response);
                // Chuyển danh sách thành danh sách có thể chỉnh sửa
                List<String> clients = new ArrayList<>(Arrays.asList(response.split(",")));

                clients.remove(username);

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

    private void handleSendFile() {
        if (selectedFile == null) {
            showAlert("Warning", "Please select a file first!");
            return;
        }

        String key = keyField.getText().trim();
        int keySize = Integer.parseInt(keySizeComboBox.getValue().replace("-bit", "").trim());
        String receiver = recipientComboBox.getValue();

        if (receiver == null || receiver.isEmpty()) {
            showAlert("Warning", "Please select a recipient!");
            return;
        }
        if (key.isEmpty()) {
            showAlert("Warning", "Please enter the encryption key!");
            return;
        }

        byte[] iv = generateRandomIV(); // Tạo IV tự động
        File encryptedFile = encryptFile(selectedFile, key, iv, keySize);
        if (encryptedFile != null) {
            sendFileToServer(encryptedFile, receiver);
            showAlert("Warning","📂 File '" + selectedFile.getName() + "' sent successfully!");
            onBack.run();
        } else {
            showAlert("Warning","Error encrypting file!");
        }
    }

    private byte[] generateRandomIV() {
        byte[] iv = new byte[16]; // IV phải có độ dài 16 byte cho AES
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) (Math.random() * 256); // Tạo giá trị ngẫu nhiên từ 0 đến 255
        }
        return iv; // Chuyển IV thành chuỗi hex để sử dụng
    }


    private File encryptFile(File file, String key, byte[] iv, int keySize) {
        try {
            if ((keySize == 128 && key.length() != 16) ||
                    (keySize == 192 && key.length() != 24) ||
                    (keySize == 256 && key.length() != 32)) {
                showAlert("Error", "❌ Key length does not match AES-" + keySize + " requirements!");
                return null;
            }

            CBC cbc = new CBC();
            File encryptDir = new File("users/" + username + "/Encrypt");
            if (!encryptDir.exists()) encryptDir.mkdirs();

            File encryptedFile = new File(encryptDir, file.getName());
            byte[] output;
            String data = dataToHexString(bytesToHex(iv)); // IV sẽ là khối đầu tiên

            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 FileOutputStream fos = new FileOutputStream(encryptedFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                bos.write(iv);

                byte[] block = new byte[16];
                int bytesRead;
                //Đọc từng khối 16 byte từ file gốc.
                while ((bytesRead = bis.read(block)) != -1) {
                    if (bytesRead < 16) {
                        for (int i = bytesRead; i < 16; i++) {
                            block[i] = (byte) (16 - bytesRead);
                        }
                    }
                    String hexString = bytesToHex(block);
                    data = cbc.encrypt(hexString, data, key);
                    output = hexToBytes(data);
                    bos.write(output);
                }
                bos.write(hexToBytes(cbc.encrypt("10101010101010101010101010101010", data, key)));
                return encryptedFile;

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            showAlert("Warning", "❌ Error during encryption: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    protected String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length*2);
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    protected byte[] hexToBytes(String hexString){
        int len = hexString.length();
        byte[] output = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            output[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }
        return output;
    }
    protected String dataToHexString(String data) {
        StringBuilder hexString = new StringBuilder();
        for (char c : data.toCharArray()) {
            hexString.append(String.format("%02X", (int) c));
        }
        return hexString.toString();
    }

    private void sendFileToServer(File file, String receiver) {
        new Thread(() -> {
            try (Socket socket = new Socket(serverIp, serverPort)) {
                DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInput = new DataInputStream(socket.getInputStream());

                // Gửi tín hiệu bắt đầu + thông tin file
                dataOutput.writeUTF("START_FILE");
                dataOutput.writeUTF(receiver);
                dataOutput.writeUTF(file.getName());
                dataOutput.writeLong(file.length());
                dataOutput.flush();

                // Gửi nội dung file
                try (FileInputStream fileInput = new FileInputStream(file)) {
                    byte[] buffer = new byte[65536]; // 64KB buffer
                    int bytesRead;
                    while ((bytesRead = fileInput.read(buffer)) != -1) {
                        dataOutput.write(buffer, 0, bytesRead);
                    }
                    dataOutput.flush();
                }

                // Nhận phản hồi từ server
                String serverResponse = dataInput.readUTF();
                System.out.println("✅ File '" + file.getName() + "' sent successfully!\nServer Response: " + serverResponse);
                Platform.runLater(() -> showAlert("Warning","✅ File '" + file.getName() + "' sent successfully!\nServer Response: " + serverResponse));
            } catch (IOException e) {
                System.err.println("❌ Error sending file: " + e.getMessage());
                Platform.runLater(() -> showAlert("Warning","❌ Error sending file: " + e.getMessage()));
            }
        }).start();
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