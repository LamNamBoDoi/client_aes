package com.example.demo1;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
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
    private final TextField keyField = new TextField();
    private final ComboBox<String> keySizeComboBox = new ComboBox<>();

    public DecryptFileScene(Stage stage, String username, Runnable onBack) {
        this.stage = stage;
        this.username = username;
        this.onBack = onBack;

        fileLabel.setEditable(false);
        fileLabel.setMaxWidth(Double.MAX_VALUE);
        keyField.setPromptText("Enter decryption key");
        keyField.setMaxWidth(Double.MAX_VALUE);
        keySizeComboBox.getItems().addAll("128-bit", "192-bit", "256-bit");
        keySizeComboBox.setValue("128-bit");
        keySizeComboBox.setMaxWidth(Double.MAX_VALUE);
    }

    public Scene createScene() {
        // Tạo layout chính với background màu nhạt
        VBox mainLayout = new VBox(15);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f7fa;");

        /* PHẦN TIÊU ĐỀ */
        Label titleLabel = new Label("DECRYPTION FILE");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3a5169;");
        titleLabel.setPadding(new Insets(0, 0, 10, 0)); // Thêm padding dưới

        /* PHẦN FORM NHẬP LIỆU */
        VBox formCard = new VBox(15);
        formCard.setMaxWidth(450);
        formCard.setPadding(new Insets(20));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        // Thiết lập GridPane cho form
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15); // Khoảng cách dọc giữa các hàng
        formGrid.setHgap(10); // Khoảng cách ngang giữa các cột
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
        Button chooseFileButton = createStyledButton("SELECT FILE", "/file.png", "#2196F3");
        chooseFileButton.setOnAction(e -> openFileChooser());

        // 2. Trường nhập key
        keyField.setStyle("-fx-padding: 8; -fx-background-color: #f8f9fa; " +
                "-fx-border-color: #d1d5db; -fx-border-width: 1; -fx-border-radius: 4;");

        // 3. Combo box chọn key size
        keySizeComboBox.setStyle("-fx-padding: 5; -fx-background-color: #f8f9fa; " +
                "-fx-border-color: #d1d5db; -fx-border-width: 1; -fx-border-radius: 4;");

        // Thêm các thành phần vào form
        addFormRow(formGrid, 0, "FILE ENCRYPTION:", fileLabel, chooseFileButton);
        addFormRow(formGrid, 1, "KEY DECRYPTION:", keyField);
        addFormRow(formGrid, 2, "KEY SIZE:", keySizeComboBox);

        /* CÁC NÚT CHỨC NĂNG */
        Button decryptButton = createStyledButton("DECRYPTION", "/unlocked.png", "#4CAF50");
        decryptButton.setStyle(decryptButton.getStyle() + "-fx-font-weight: bold;");
        decryptButton.setOnAction(e -> decryptFile());

        Button backButton = createStyledButton("BACK", "/back.png", "#616161");
        backButton.setOnAction(e -> onBack.run());

        // Container cho các nút
        HBox buttonBox = new HBox(15, backButton, decryptButton);
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
        int keySize = Integer.parseInt(keySizeComboBox.getValue().replace("-bit", "").trim());

        if ((keySize == 128 && key.length() != 16) ||
                (keySize == 192 && key.length() != 24) ||
                (keySize == 256 && key.length() != 32)) {
            showErrorAlert("Error", "❌ Key length does not match AES-" + keySize + " requirements!");
            return;
        }

        File decryptDir = new File("users/" + username + "/Decrypted");
        if (!decryptDir.exists()) decryptDir.mkdirs();


        File decryptedFile = new File(decryptDir,  selectedFile.getName());
        try {
            CBC cbc = new CBC();
            byte[] output;
            String previousHexString = "";
            String result = "";
            String hexString = "";
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 FileOutputStream fos = new FileOutputStream(decryptedFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos))
            {
                byte[] iv = new byte[16];
                if (bis.read(iv) != iv.length) {
                    showErrorAlert("Lỗi tệp", "Không thể đọc IV từ tệp.");
                    fis.close();
                    return;
                }
                String data = dataToHexString(bytesToHex(iv));
                byte[] block = new byte[16];
                byte[] previousBlock;
                byte[] previousDecrypt;
                int bytesRead;
                bytesRead = bis.read(block);
                if (bytesRead == -1) {
                    // File không có đủ dữ liệu để đọc
                    return;
                }
                previousBlock = block;
                previousHexString = data;
                hexString = bytesToHex(block);
                result=cbc.decrypt(hexString, previousHexString, key);
                previousDecrypt = hexToBytes(result);
                previousHexString = bytesToHex(previousBlock);
                // nguyên lý cbc
                // block đầu: giải mã aes -> xor với IV -> ra plaintext
                // block sau: Giải mã AES → XOR với ciphertext block trước → Ra plaintext
                while (bis.read(block) != -1) {
                    // Nếu không phải là khối đầu tiên, chuyển đổi khối trước đó thành chuỗi hexa và in ra
                    // Convert block to hex string
                    hexString = bytesToHex(block);

                    // Convert hex string back to byte array
                    result=cbc.decrypt(hexString, previousHexString, key);
                    output = hexToBytes(result);
                    // Kiểm tra xem chuỗi có chứa PKCS7 padding không
                    if(Arrays.equals(output, new byte[]{0x10, 0x10, 0x10, 0x10, 0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10})){
                        previousDecrypt = removePadding(previousDecrypt);
                        bos.write(previousDecrypt);
                    }else{
                        bos.write(previousDecrypt);
                    }
                    //System.arraycopy(source, srcPos, destination, destPos, length);
                    System.arraycopy(block, 0, previousBlock, 0, block.length);
                    previousHexString = bytesToHex(previousBlock);
                    previousDecrypt = output;
                }
                bos.close();
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            showSuccessAlert("Giải mã thành công", "Tệp đã được giải mã và lưu tại:\n" + decryptedFile.getAbsolutePath());

            // Mở tệp sau khi giải mã thành công
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(decryptedFile);
            }
        } catch (Exception e) {
            showErrorAlert("Giải mã thất bại", "Lỗi: " + e.getMessage());
        }
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
    public byte[] removePadding(byte[] output) {
        int paddingLength = output[output.length - 1];
        if (paddingLength > 0 && paddingLength <= 16) {
            // Loại bỏ byte padding
            for (int i = 16 - paddingLength; i < 15; i++) {
                if (output[i] != paddingLength) {
                    return output;
                }
            }
            byte[] result = new byte[output.length - paddingLength];
            System.arraycopy(output, 0, result, 0, result.length);
            return result;
        } else {
            // Nếu không có padding, trả về chuỗi ban đầu
            return output;
        }
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