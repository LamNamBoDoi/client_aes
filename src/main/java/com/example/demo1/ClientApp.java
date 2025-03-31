package com.example.demo1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class ClientApp extends Application {
    private Socket socket;
    private DataOutputStream dataOutput;
    private DataInputStream dataInput;
    private final TextArea logArea = new TextArea();
    private String currentUsername;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AES Transfer Client");
        primaryStage.setScene(createLoginScene(primaryStage));
        primaryStage.setResizable(false);
        primaryStage.setMaximized(false);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/client.png"))));
        primaryStage.show();
    }

    private Scene createLoginScene(Stage stage) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10.0);
        gridPane.setVgap(10.0);
        gridPane.setPadding(new Insets(10.0));
        gridPane.setAlignment(Pos.CENTER);

        ImageView logoView = new ImageView();
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
            logoView.setImage(logo);
            logoView.setFitHeight(60);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        Label titleLabel = new Label("AES TRANSFER CLIENT");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        StackPane logoContainer = new StackPane(logoView);
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPadding(new Insets(30.0, 0.0, 20.0, 0.0));

        Label ipLabel = new Label("IP ⚡:");
        ipLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        TextField ipField = new TextField("127.0.0.1");
        ipField.setPrefWidth(200.0);

        Label portLabel = new Label("PORT:");
        portLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        TextField portField = new TextField("5000");
        portField.setPrefWidth(200.0);

        Label userLabel = new Label("Name:");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPrefWidth(200.0);

        Button loginButton = new Button("Login");
        loginButton.setMinWidth(260.0);
        loginButton.setOnAction(event -> {
            String ip = ipField.getText().trim();
            String portText = portField.getText().trim();
            String username = userField.getText().trim();
            //Kiểm tra dữ liệu nhập vào
            if (ip.isEmpty() || portText.isEmpty()) {
                showAlert("Please enter the full IP and PORT!");
                return;
            }

            try {
                int port = Integer.parseInt(portText);
                if (port <= 0 || port > 65535) {
                    showAlert("Invalid PORT!");
                    return;
                }

                if (username.isEmpty()) {
                    showAlert("Please enter a Username!");
                    return;
                }
                // Kết nối đến server
                connectToServer(ip, port, username, stage);
            } catch (NumberFormatException e) {
                showAlert("Invalid PORT format!");
            }
        });

        HBox buttonContainer = new HBox(loginButton);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10.0, 0.0, 20.0, 0.0));

        gridPane.add(ipLabel, 0, 0);
        gridPane.add(ipField, 1, 0);
        gridPane.add(portLabel, 0, 1);
        gridPane.add(portField, 1, 1);
        gridPane.add(userLabel, 0, 2);
        gridPane.add(userField, 1, 2);
        gridPane.add(buttonContainer, 0, 3, 2, 1);

        VBox vbox = new VBox(logoContainer, titleLabel, gridPane);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);


        StackPane layout = new StackPane(vbox);
        Scene scene = new Scene(layout, 350.0, 400.0);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        return scene;
    }

    private Scene createMainScene(Stage stage) {
        // Nút my files
        ImageView fileIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/myfile.png"))));
        fileIcon.setFitWidth(24.0);
        fileIcon.setFitHeight(24.0);

        Button fileButton = new Button("My Files của "+currentUsername, fileIcon);
        fileButton.setMaxWidth(Double.MAX_VALUE);
        fileButton.getStyleClass().add("myfile-button");
        fileButton.setOnAction(event -> {
            if (currentUsername != null) {
                File userDecryptDir = new File("./users/" + currentUsername + "/Decrypted");
                if (!userDecryptDir.exists()) {
                    boolean mkdirs = userDecryptDir.mkdirs();
                }

                try {
                    Desktop.getDesktop().open(userDecryptDir);
                } catch (IOException e) {
                    showAlert("Cannot open the folder!");
                }
            } else {
                showAlert("Not logged in!");
            }
        });

        // Nút send file
        ImageView sendIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/send.png"))));
        sendIcon.setFitWidth(24.0);
        sendIcon.setFitHeight(24.0);

        Button sendButton = new Button("Send File", sendIcon);
        sendButton.setMaxWidth(Double.MAX_VALUE);
        sendButton.getStyleClass().add("sendfile-button");
        sendButton.setOnAction(event -> {
            stage.setScene(new SendFileScene(
                    stage,
                    currentUsername,
                    socket.getInetAddress().getHostAddress(),
                    socket.getPort(),
                    () -> stage.setScene(createMainScene(stage))
            ).createScene());
        });

        ImageView decryptIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/decryption.png"))));
        decryptIcon.setFitWidth(24.0);
        decryptIcon.setFitHeight(24.0);

        Button decryptButton = new Button("Decrypt File", decryptIcon);
        decryptButton.setMaxWidth(Double.MAX_VALUE);
        decryptButton.getStyleClass().add("decryptfile-button");
        decryptButton.setOnAction(event -> {
            if (currentUsername != null) {
                stage.setScene(new DecryptFileScene(stage, currentUsername, () ->
                        stage.setScene(createMainScene(stage))
                ).createScene());
            } else {
                showAlert("Not logged in!");
            }
        });

        ImageView logoutIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logout.png"))));
        logoutIcon.setFitWidth(24.0);
        logoutIcon.setFitHeight(24.0);

        Button logoutButton = new Button("Logout", logoutIcon);
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(event -> {
            disconnectFromServer();
            stage.setScene(createLoginScene(stage));
        });

        VBox buttonBox = new VBox(15.0, fileButton, sendButton, decryptButton, logoutButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(30.0));
        buttonBox.setPrefWidth(300.0);

        StackPane root = new StackPane(buttonBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50.0));

        Scene scene = new Scene(root, 400.0, 300.0);
        scene.getStylesheets().add(getClass().getResource("/style2.css").toExternalForm());

        return scene;
    }

    private void connectToServer(String ip, int port, String username, Stage stage) {
        // Tác vụ bất đồng
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    socket = new Socket(ip, port);
                    dataOutput = new DataOutputStream(socket.getOutputStream());
                    dataInput = new DataInputStream(socket.getInputStream());

                    // Gửi yêu cầu đăng nhập
                    dataOutput.writeUTF("LOGIN:" + username);
                    currentUsername = username;

                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        };

        // được gọi khi tác vụ hoàn thành
        task.setOnSucceeded(event -> {
            if (task.getValue()) {
                Platform.runLater(() -> {
                    appendLog("Connected as " + username);
                    stage.setScene(createMainScene(stage));
                });
                listenToServer();
            } else {
                showAlert("Cannot connect to the server!");
            }
        });

        new Thread(task).start();
    }

    private void listenToServer() {
        new Thread(() -> {
            FileReceiver fileReceiver = null;
            try {
                fileReceiver = new FileReceiver(socket, currentUsername);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                while (true) {
                    String serverMessage = dataInput.readUTF();
                    if (serverMessage.isEmpty()) break;

                    Platform.runLater(() -> appendLog("📩 Server: " + serverMessage));

                    if ("FILE".equals(serverMessage)) {
                        String fileName = dataInput.readUTF().trim();
                        if (fileName.isEmpty()) break;

                        long fileSize = dataInput.readLong();

                        Platform.runLater(() ->
                                showAlert("📂 You received a file: \"" + fileName + "\" (" + fileSize + " bytes)"));

                        fileReceiver.receiveFile(fileName, fileSize);

                        Platform.runLater(() -> appendLog("✅ File " + fileName + " received."));
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> appendLog("❌ Lost connection to the server!"));
            } finally {
                disconnectFromServer();
            }
        }).start();
    }

    private void disconnectFromServer() {
        try {
            if (socket != null) {
                socket.close();
            }
            appendLog("Disconnected");
        } catch (IOException e) {
            appendLog("Error disconnecting: " + e.getMessage());
        }
    }

    private void appendLog(String text) {
        Platform.runLater(() -> logArea.appendText(text + "\n"));
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}