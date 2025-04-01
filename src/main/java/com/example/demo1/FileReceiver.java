package com.example.demo1;

import java.io.*;
import java.net.Socket;

// Nhận các tệp tin và lưu vào hệ thống
public class FileReceiver {
    private final String username;
    private final DataInputStream dataInput;

    public FileReceiver(Socket socket, String username) throws IOException {
        this.username = username;
        this.dataInput = new DataInputStream(socket.getInputStream());
    }

    public void receiveFile(String fileName, long fileSize) {
        // tạo thư mục lưu trữ tệp
        File receivedDir = new File("./users/" + username + "/Received");
        if (!receivedDir.exists()) {
            receivedDir.mkdirs();
        }
        // tạo file
        File file = new File(receivedDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[65536]; // Buffer 64KB giúp truyền nhanh hơn
            long totalRead = 0;

            while (totalRead < fileSize) {
                int bytesRead = dataInput.read(buffer);
                if (bytesRead == -1) {
                    throw new IOException("Connection lost while receiving file!");
                }
                // 0 offset của buffer
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                // Log tiến trình
                System.out.println("\uD83D\uDCE5 Receiving " + fileName + ": " + ((totalRead * 100) / fileSize) + "%");
            }

            // Kiểm tra nếu file bị mất dữ liệu
            if (file.length() != fileSize) {
                throw new IOException("Incomplete file! Expected " + fileSize + " bytes, got " + file.length() + " bytes");
            }

            System.out.println("✅ File received successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("❌ Error receiving file: " + e.getMessage());
            file.delete(); // Xóa file bị lỗi để tránh hỏng dữ liệu
        }
    }
}
