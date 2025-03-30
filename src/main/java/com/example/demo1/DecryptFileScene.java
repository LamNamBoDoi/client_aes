package com.example.demo1;

import java.io.File;
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

public class DecryptFileScene {
    private final Stage stage;
    private final String username;
    private final Runnable onBack;
    private File selectedFile;

    private final TextField fileLabel = new TextField("No file selected.");
    private final TextField keyField = new TextField();
    private final ComboBox<String> keySizeComboBox = new ComboBox<>();

    public DecryptFileScene(Stage stage, String username, Runnable onBack) {
        this.stage = stage;
        this.username = username;
        this.onBack = onBack;

        fileLabel.setEditable(false);
        fileLabel.setMaxWidth(300);
        keyField.setPromptText("Enter key");
        keyField.setMaxWidth(300);
        keySizeComboBox.getItems().addAll("128", "192", "256");
        keySizeComboBox.setValue("128");
        keySizeComboBox.setMaxWidth(240);
    }

    public Scene createScene() {
        Button backButton = new Button("Back", loadImage("/left-arrow.png"));
        backButton.setOnAction(e -> onBack.run());
        HBox backButtonContainer = new HBox(backButton);
        backButtonContainer.setAlignment(Pos.TOP_LEFT);
        backButtonContainer.setPadding(new Insets(10, 0, 20, 0));

        Button chooseFileButton = new Button("Select file", loadImage("/icons/data-encryption.png"));
        chooseFileButton.setMaxWidth(Double.MAX_VALUE);
        chooseFileButton.setOnAction(e -> openFileChooser());

        Button decryptButton = new Button("Decrypt", loadImage("/unlocked.png"));
        decryptButton.setMaxWidth(Double.MAX_VALUE);
        decryptButton.setOnAction(e -> decryptFile());

        VBox layout = new VBox(12, backButtonContainer, fileLabel, keyField, keySizeComboBox, chooseFileButton, decryptButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        StackPane root = new StackPane(layout);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 300, 400);
        scene.getStylesheets().add(getClass().getResource("/destyles.css").toExternalForm());
        return scene;
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        File receivedDir = new File("users/" + username + "/Received");
        if (!receivedDir.exists()) receivedDir.mkdirs();
        fileChooser.setInitialDirectory(receivedDir);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            fileLabel.setText(file.getName());
        }
    }

    private void decryptFile() {
        if (selectedFile == null || keyField.getText().trim().isEmpty() || keySizeComboBox.getValue() == null) {
            showAlert("Please select a file and enter a key!");
            return;
        }
        File decryptDir = new File("users/" + username + "/Decrypt");
        if (!decryptDir.exists()) decryptDir.mkdirs();
        File decryptedFile = new File(decryptDir, selectedFile.getName());

        // Fake decryption process
        boolean success = true;

        if (success) {
            showAlert("✅ File has been decrypted successfully!\nSaved at: " + decryptedFile.getAbsolutePath());
            try {
//                if (Desktop.isDesktopSupported()) {
//                    Desktop.getDesktop().open(decryptedFile);
//                } else {
//                    showAlert("Your system does not support automatic file opening.");
//                }
            } catch (Exception e) {
                showAlert("Cannot open file: " + e.getMessage());
            }
        } else {
            showAlert("❌ Decryption failed!");
            decryptedFile.delete();
        }
    }

    private void showAlert(String message) {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait());
    }

    private ImageView loadImage(String path) {
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            return new ImageView(image);
        } catch (Exception e) {
            System.err.println("Image not found: " + path);
            return new ImageView();
        }
    }
}