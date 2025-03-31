package com.example.demo1;

import com.example.demo1.AES;

// Chế độ CBC (Cipher Block Chaining)
// Mỗi khối plaintext được XOR với ciphertext khối trước đó trước khi mã hóa.
// plaintext: dữ liệu gốc trước khi mã hóa
// ciphertext: dữ liệu đã được mã hóa từ plaintext
public class CBC extends AES {

    // Hàm mã hóa dữ liệu đầu vào bằng AES-CBC
    @Override
    public String encrypt(String input, String iv, String key) {
        int[][] state = new int[4][4]; // Ma trận trạng thái chứa dữ liệu
        int[][] previous = new int[4][4]; // Ma trận lưu IV (Initialization Vector)
        String output = "";

        key = previousToHexString(key); // Chuyển key thành chuỗi hex
        int loop = (key.length() * 4) / 32 + 6; // Xác định số vòng lặp dựa trên độ dài key

        // Chuyển đổi chuỗi input và IV thành ma trận 4x4
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                // Chuyển input từ hex string sang ma trận state
                state[k][j] = Integer.parseInt(input.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
                // Chuyển IV từ hex string sang ma trận previous
                previous[k][j] = Integer.parseInt(iv.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
            }
        }

        // Mở rộng khóa AES
        this.expandedKey = this.keyExpansion(key);

        // XOR IV với state (áp dụng XOR lần đầu tiên với IV)
        state = xorIV(state, previous);

        // Thêm khóa vòng đầu tiên
        state = this.addRoundKey(state, 0);

        // Thực hiện các vòng mã hóa
        for (int j = 1; j < loop; j++) {
            state = this.subBytes(state);
            state = this.shiftRows(state);
            state = this.mixColumns(state);
            state = this.addRoundKey(state, j);
        }

        // Vòng cuối cùng không có mixColumns
        state = this.subBytes(state);
        state = this.shiftRows(state);
        state = this.addRoundKey(state, loop);

        // Chuyển ma trận state thành chuỗi đầu ra
        output += this.toString(state);
        return output;
    }

    // Hàm giải mã dữ liệu đầu vào bằng AES-CBC
    @Override
    public String decrypt(String input, String iv, String key) {
        int[][] state = new int[4][4]; // Ma trận trạng thái chứa dữ liệu đã mã hóa
        int[][] previous = new int[4][4]; // Ma trận lưu IV (Initialization Vector)
        String output = "";

        key = previousToHexString(key); // Chuyển key thành chuỗi hex
        int loop = (key.length() * 4) / 32 + 6; // Xác định số vòng lặp dựa trên độ dài key

        // Chuyển đổi chuỗi input và IV thành ma trận 4x4
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                state[k][j] = Integer.parseInt(input.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
                previous[k][j] = Integer.parseInt(iv.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
            }
        }

        // Mở rộng khóa AES
        this.expandedKey = this.keyExpansion(key);

        // Thêm khóa vòng đầu tiên (ngược với mã hóa)
        state = this.addRoundKey(state, loop);

        // Thực hiện các vòng giải mã
        for (int j = loop - 1; j > 0; j--) {
            state = this.invshiftRows(state);
            state = this.invsubBytes(state);
            state = this.addRoundKey(state, j);
            state = this.invmixColumns(state);
        }

        // Vòng cuối cùng không có invMixColumns
        state = this.invshiftRows(state);
        state = this.invsubBytes(state);
        state = this.addRoundKey(state, 0);

        // XOR IV để khôi phục dữ liệu gốc
        state = xorIV(state, previous);

        // Chuyển ma trận state thành chuỗi đầu ra
        output += this.toString(state);
        return output;
    }

    // Hàm thực hiện XOR giữa state và IV
    private int[][] xorIV(int[][] state, int[][] iv) {
        int[][] tmp = new int[4][4];
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                tmp[k][j] = state[k][j] ^ iv[k][j]; // Thực hiện phép XOR bitwise
            }
        }
        return tmp;
    }

    // Hàm chuyển đổi chuỗi ký tự thành chuỗi hex
    private String previousToHexString(String previousString) {
        StringBuilder hexString = new StringBuilder();
        for (char c : previousString.toCharArray()) {
            hexString.append(String.format("%02X", (int) c));
        }
        return hexString.toString();
    }
}
