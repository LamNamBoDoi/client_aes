package com.example.demo1;

import com.example.demo1.AES;
// cipher block chaining
// CBC Mode: Mỗi block plaintext được XOR với ciphertext block trước đó trước khi mã hóa
// plaintext dữ liệu gốc trước khi mã hóa
// ciphertext là dữ liệu đã được mã hóa từ plaintext
public class CBC extends AES {
    @Override
    public String encrypt(String input, String iv, String key) {
        // ma trận lưu dữ liệu cần mã hóa
        int[][] state = new int[4][4];
        // ma trận lưu IV
        int[][] previous = new int[4][4];
        String output = "";

        // chuyển chuỗi thành hex
        key = previousToHexString(key);
        int loop=(key.length()*4)/32+6;
        // chuyển input và iv từ hex thành ma trận
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                // chuỗi input từ hex string sang ma trận state(cắt 2 ký tự -> chuyển hex sang số nguyên)
                state[k][j] = Integer.parseInt(input.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
                // // Chuyển IV từ hex string sang ma trận previous
                previous[k][j] = Integer.parseInt(iv.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
            }
        }
        // mở rộng khóa
        this.expandedKey = this.keyExpansion(key);

        // XOR IV
        state = xorIV(state, previous);

        // Add round key - round 0
        state = this.addRoundKey(state, 0);

        // Iterate for 10 rounds
        for (int j = 1; j < loop; j++) {
            state = this.subBytes(state);
            state = this.shiftRows(state);
            state = this.mixColumns(state);
            state = this.addRoundKey(state, j);
        }

        // Final round
        state = this.subBytes(state);
        state = this.shiftRows(state);
        state = this.addRoundKey(state, loop);



        // Add state to output string
        output += this.toString(state);
        return output;
    }

    @Override
    public String decrypt(String input, String iv, String key) {
        int[][] state = new int[4][4];
        int[][] previous = new int[4][4];
        String output = "";

        key = previousToHexString(key);
        int loop=(key.length()*4)/32+6;
//        System.out.println("---------------");
//        System.out.println("iv: "+iv);
//        System.out.println("---------------");
        // Parse string into 4 x 4 state
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                state[k][j] = Integer.parseInt(input.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
                previous[k][j] = Integer.parseInt(iv.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
            }
        }

        this.expandedKey = this.keyExpansion(key);

        // Add round key - round 0
        state = this.addRoundKey(state, loop);

        // Iterate for 10 rounds
        for (int j = loop-1; j >0; j--) {

            state = this.invshiftRows(state);
            state = this.invsubBytes(state);
            state = this.addRoundKey(state, j);
            state = this.invmixColumns(state);
        }

        // Final round

        state = this.invshiftRows(state);
        state = this.invsubBytes(state);
        state = this.addRoundKey(state, 0);

        // XOR IV
        state = xorIV(state, previous);

        // Add state to output string
        output += this.toString(state);
        return output;
    }

    private int[][] xorIV(int[][] state, int[][] iv) {
        int[][] tmp = new int[4][4];

        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                tmp[k][j] = state[k][j] ^ iv[k][j];
            }
        }

        return tmp;
    }

    // chuyển đổi chuỗi ký tự thành hex
    private String previousToHexString(String previousString) {
        StringBuilder hexString = new StringBuilder();
        for (char c : previousString.toCharArray()) {
            hexString.append(String.format("%02X", (int) c));
        }
        return hexString.toString();
    }
}
