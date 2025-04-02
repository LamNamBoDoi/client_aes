package com.example.demo1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private final TextField fileLabel = new TextField("No file selected");
    private final TextField keyField;
    private final ComboBox<String> recipientComboBox;
    private final ComboBox<String> keySizeComboBox;

    public SendFileScene(Stage stage, String username, String serverIp, int serverPort, Runnable onBack) {
        this.stage = stage;
        this.username = username;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.onBack = onBack;

        fileLabel.setEditable(false);
        fileLabel.setMaxWidth(Double.MAX_VALUE);

        keyField = new TextField();
        keyField.setPromptText("Enter encryption key");
        keyField.setMaxWidth(Double.MAX_VALUE);

        recipientComboBox = new ComboBox<>();
        recipientComboBox.setPromptText("Select recipient");
        recipientComboBox.setMaxWidth(Double.MAX_VALUE);

        keySizeComboBox = new ComboBox<>();
        keySizeComboBox.getItems().addAll("128-bit", "192-bit", "256-bit");
        keySizeComboBox.setValue("128-bit");
        keySizeComboBox.setMaxWidth(Double.MAX_VALUE);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
    }

    public Scene createScene() {
        requestClientList(); // Request list of currently connected clients

        // Tạo layout chính với background màu nhạt
        VBox mainLayout = new VBox(15);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f7fa;");

        /* TITLE SECTION */
        Label titleLabel = new Label("SEND ENCRYPTED FILE");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3a5169;");

        /* FORM SECTION */
        VBox formCard = new VBox(15);
        formCard.setMaxWidth(450);
        formCard.setPadding(new Insets(20));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        // Thiết lập GridPane cho form
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(10);
        formGrid.setPadding(new Insets(10));

        // Cấu hình cột
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(120);
        labelCol.setPrefWidth(120);
        labelCol.setHgrow(Priority.NEVER);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);

        formGrid.getColumnConstraints().addAll(labelCol, fieldCol);

        /* CÁC TRƯỜNG NHẬP LIỆU */
        // 1. Trường chọn file
        fileLabel.setStyle("-fx-padding: 8; -fx-background-color: #f8f9fa; " +
                "-fx-border-color: #d1d5db; -fx-border-width: 1; -fx-border-radius: 4;");

        // 2. Trường nhập key
        keyField.setStyle("-fx-padding: 8; -fx-background-color: #f8f9fa; " +
                "-fx-border-color: #d1d5db; -fx-border-width: 1; -fx-border-radius: 4;");

        // 3. Combo box chọn key size
        keySizeComboBox.setStyle("-fx-padding: 5; -fx-background-color: #f8f9fa; " +
                "-fx-border-color: #d1d5db; -fx-border-width: 1; -fx-border-radius: 4;");

        // 4. Combo box chọn key size
        recipientComboBox.setStyle("-fx-padding: 5; -fx-background-color: #f8f9fa; " +
                "-fx-border-color: #d1d5db; -fx-border-width: 1; -fx-border-radius: 4;");


        /* CÁC NÚT CHỨC NĂNG */
        Button chooseFileButton = createStyledButton("SELECT FILE", "/file.png", "#2196F3");
        chooseFileButton.setOnAction(e -> openFileChooser());

        Button sendButton = createStyledButton("SEND", "/send.png", "#28a745");
        sendButton.setOnAction(e -> handleSendFile());

        Button backButton = createStyledButton("BACK", "/back.png", "#616161");
        backButton.setOnAction(e -> onBack.run());

        Button refreshButton = createStyledButton("Refresh", "/refresh.png", "#6f42c1");
        refreshButton.setOnAction(e -> requestClientList());

        // Thêm các thành phần vào form
        addFormRow(formGrid, 0, "SELECT FILE:", fileLabel, chooseFileButton);
        addFormRow(formGrid, 1, "KEY ENCRYPTION:", keyField, null);
        addFormRow(formGrid, 2, "KEY SIZE:", keySizeComboBox,null);
        addFormRow(formGrid, 3, "RECIPIENT: ", recipientComboBox, refreshButton);

        // Container cho các nút
        HBox buttonBox = new HBox(15, backButton, sendButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // Thêm các thành phần vào form card
        formCard.getChildren().addAll(formGrid, new Separator(), buttonBox);

        // Thêm tất cả vào layout chính
        mainLayout.getChildren().addAll(titleLabel, formCard);

        // Thiết lập kích thước ưu tiên
        mainLayout.setPrefSize(500, 350);

        return new Scene(mainLayout);
    }
    private void openFileChooser() {
        // người dùng chọn file từ hệ thống
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getName());
        }
    }
    // Phương thức hỗ trợ tạo nút có style
    private Button createStyledButton(String text, String iconPath, String color) {
        Button button = new Button(text, loadImage(iconPath, 16, 16));
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8 15; " +
                "-fx-background-radius: 4;");

        // Hiệu ứng khi di chuột vào
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", -15%); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 15; " +
                        "-fx-background-radius: 4;"));

        // Hiệu ứng khi di chuột ra
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 15; " +
                        "-fx-background-radius: 4;"));

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
        if ((keySize == 128 && key.length() != 16) ||
                (keySize == 192 && key.length() != 24) ||
                (keySize == 256 && key.length() != 32)) {
            showAlert("Error", "❌ Key length does not match AES-" + keySize + " requirements!");
            return;
        }

        byte[] iv = generateRandomIV(); // Tạo IV tự động
        File encryptedFile = encryptFile(selectedFile, key, iv);
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


    private File encryptFile(File file, String key, byte[] iv) {
        try {
            CBC cbc = new CBC();
            // Khởi tạo thư mục lưu tập tin đã mã hóa
            File encryptDir = new File("users/" + username + "/Encrypt");
            if (!encryptDir.exists()) encryptDir.mkdirs();

            // tạo file mưu trữ mã hóa
            File encryptedFile = new File(encryptDir, file.getName());
            byte[] output;
            String data = dataToHexString(bytesToHex(iv));

            // buffer giảm số lần truy cập I/O
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
                // GHI KHỐI MAC ĐỂ KIỂM TRA TÍNH TOÀN VẸN
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