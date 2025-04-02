package com.example.demo1;

public abstract class AES {
    protected int[][] expandedKey;

    public int[][] subBytes(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                int hex = arr[i][j];
                // Lấy phần nguyên của hex chia 16
                // Lấy phần dư của hex chia 16
                arr[i][j] = Constants.sbox[hex / 16][hex % 16];  // Thay thế giá trị ban đầu bằng giá trị từ bảng sbox
            }
        }
        return arr;
    }

    public int[][] invsubBytes(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                int hex = arr[i][j];
                // Lấy phần nguyên của hex chia 16
                // Lấy phần dư của hex chia 16
                arr[i][j] = Constants.rsbox[hex / 16][hex % 16];  // Thay thế giá trị ban đầu bằng giá trị từ bảng sbox
            }
        }
        return arr;
    }

    public int[] leftRotate(int[] arr, int shiftAmount)
    {
        if (shiftAmount % 4 == 0) {
            System.out.println("Shift Amount <4");
        }
        while (shiftAmount > 0) {
            int temp = arr[0];
            for (int i = 0; i < arr.length - 1; i++) {
                arr[i] = arr[i + 1];
            }
            arr[arr.length - 1] = temp;
            shiftAmount--;
        }
        return arr;
    }
    public int[][] shiftRows(int[][] arr) {
        // Dịch trái các hàng từ hàng thứ 1 đến hàng cuối của state
        for (int row = 1; row < 4; row++) {
            // Dịch trái hàng thứ row
            arr[row] = leftRotate(arr[row], row);
        }
        return arr;
    }

    public int[] rightRotate(int[] arr, int shiftAmount)
    {

        if (shiftAmount % 4 == 0) {
            System.out.println("Shift Amount <4");
        }
        while (shiftAmount > 0) {
            int temp = arr[arr.length - 1];
            for (int i = arr.length - 1; i >0; i--) {
                arr[i] = arr[i - 1];
            }
            arr[0] = temp;
            shiftAmount--;
        }
        return arr;
    }
    public int[][] invshiftRows(int[][] arr) {
        // Dịch trái các hàng từ hàng thứ 1 đến hàng cuối của state
        for (int row = 1; row < 4; row++) {
            // Dịch trái hàng thứ row
            arr[row] = rightRotate(arr[row], row);
        }
        return arr;
    }

    // Hàm thực hiện phép XOR giữa state và round key
    public int[][] addRoundKey(int[][] state, int round) {
        int[][] roundKey = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                roundKey[j][i] = this.expandedKey[j][(4 * round) + i];
            }
        }

        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                // Thực hiện phép XOR giữa mỗi phần tử của state và round key tương ứng
                state[j][i] ^= roundKey[j][i];
            }
        }
        return state;
    }

    public int[][] mixColumns(int[][] state) //method for mixColumns
    {
        int[][] tempstate = new int[4][4];
        for(int i = 0; i < 4; i++)
        {
            System.arraycopy(state[i], 0, tempstate[i], 0, 4);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = mcHelper(tempstate, Constants.galois, i, j);
            }
        }
        return state;
    }

    private int mcHelper(int[][] arr, int[][] galois, int i, int j)
    {
        int mcsum = 0;
        for (int k = 0; k < 4; k++) {
            int a = galois[i][k];
            int b = arr[k][j];
            mcsum ^= mcCalc(a, b);
        }
        return mcsum;
    }

    private int mcCalc(int a, int b) //Helper method for mcHelper
    {
        if (a == 1) {
            return b;
        } else if (a == 2) {
            return Constants.mc2[b / 16][b % 16];
        } else if (a == 3) {
            return Constants.mc3[b / 16][b % 16];
        }
        return 0;
    }

    public int[][] invmixColumns(int[][] state) //method for mixColumns
    {
        int[][] tempstate = new int[4][4];
        for(int i = 0; i < 4; i++)
        {
            System.arraycopy(state[i], 0, tempstate[i], 0, 4);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = invmcHelper(tempstate, Constants.invgalois, i, j);
            }
        }
        return state;
    }

    private int invmcHelper(int[][] arr, int[][] invgalois, int i, int j)
    {
        int mcsum = 0;
        for (int k = 0; k < 4; k++) {
            int a = invgalois[i][k];
            int b = arr[k][j];
            mcsum ^= invmcCalc(a, b);
        }
        return mcsum;
    }

    private int invmcCalc(int a, int b) //Helper method for mcHelper
    {
        if (a == 9) {
            return Constants.mc9[b / 16][b % 16];
        } else if (a == 0xb) {
            return Constants.mc11[b / 16][b % 16];
        } else if (a == 0xd) {
            return Constants.mc13[b / 16][b % 16];
        } else if (a == 0xe) {
            return Constants.mc14[b / 16][b % 16];
        }
        return 0;
    }

    public int[] schedule_core(int[] in, int rconpointer) {
        in = leftRotate(in, 1);
        int hex;
        for (int i = 0; i < in.length; i++) {
            hex = in[i];
            in[i] = Constants.sbox[hex / 16][hex % 16];
        }
        in[0] ^= Constants.rcon[rconpointer];
        return in;
    }



    protected int[][] keyExpansion(String key) {
        // kích thước khóa
        int binkeysize = key.length() * 4;

        // tổng kích thước khóa mở rộng
        int colsize = binkeysize + 48 - (32 * ((binkeysize / 64) - 2));
        // số word
        int keySize = colsize/4;
        int nk = binkeysize/32;
        int rconIndex = 1;

        // mảng khóa mở rông
        int[][] expandedKey = new int[4][keySize];
        // Bước 1: Điền khóa gốc vào các cột đầu tiên
        for (int i = 0; i < nk; i++) {
            for (int j = 0; j < 4; j++) {
                // chuyển hex về số nguyên
                expandedKey[j][i] = Integer.parseInt(key.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
            }
        }

        // Bắt đầu mở rộng từ vị trí sau khóa gốc
        int current = nk;
        int[] a = new int[4];
        int b;
        // Bước 2: Mở rộng khóa
        while (current < keySize) {
            // Trường hợp 1: Cần áp dụng hàm schedule_core (mỗi nk từ)
            if (current % nk == 0) {
                // Sao chép word cuối cùng vào mảng tạm
                for (b = 0; b < 4; b++) {
                    a[b] = expandedKey[b][current - 1];
                }
                // Áp dụng hàm schedule_core (RotWord + SubWord + Rcon)
                a = schedule_core(a, rconIndex++);
                // XOR với từ [current-nk]
                for (b = 0; b < 4; b++) {
                    expandedKey[b][current] = a[b] ^ expandedKey[b][current - nk];
                }
                // Trường hợp 2: Xử lý đặc biệt cho AES-256 (nk=8)
            } else if(current % nk ==4 && nk==8){
                for (b = 0; b < 4; b++) {
                    int hex = expandedKey[b][current - 1];
                    expandedKey[b][current] = Constants.sbox[hex / 16][hex % 16] ^ expandedKey[b][current - nk];
                }
            }
            else {
                // Trường hợp 3: XOR đơn giản với từ [current-nk]
                for (b = 0; b < 4; b++) {
                    expandedKey[b][current] = expandedKey[b][current - 1] ^ expandedKey[b][current - nk];
                }
            }
            current++;
        }

        return expandedKey;
    }

   /* public void printState(int[][] state) {
        for (int i = 0; i < state.length; i++) {

            for (int j = 0; j < state[i].length; j++) {
                System.out.printf("%02X ", state[i][j]); // In giá trị của mỗi byte, với định dạng Hexadecimal
            }
            System.out.println(); // Xuống dòng sau khi in xong mỗi hàng
        }
    }*/

    protected String toString(int[][] state) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                String k = Integer.toHexString(state[j][i]);
                if (k.length() == 1) {
                    output.append('0').append(k);
                } else {
                    output.append(k);
                }
                // Append a space
                //       output += ' ';
            }
        }
        return output.toString();
    }

    public abstract String encrypt(String input, String iv, String key);

    public abstract String decrypt(String input, String iv, String key);


}
