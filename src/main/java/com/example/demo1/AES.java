package com.example.demo1;

public abstract class AES {
    // Holds the expanded Key
    protected int[][] expandedKey;
    // Holds the initialization vector.
  //  protected int[][] initializationVector = new int[4][4];

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

    /**
     * Performed by mapping each element in the current matrix with the value
     * returned by its helper function.
    // * @param //arr the array with we calculate against the galois field matrix.
     */

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

    /**
     * Helper method of mixColumns in which compute the mixColumn formula on each element.
     * @param arr passed in current matrix
    // * @param g the galois field
     * @param i the row position
     * @param j the column position
     * @return the computed mixColumns value
     */

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



    /**
     * For every (binary key size / 32)th column in the expanded key. We compute a special column
     * using sbox and an XOR of the an rcon number with the first element in the passed array.
     *
  //   * @param in the array in which we compute the next set of bytes for key expansion
   //  * @param rconpointer the element in the rcon array with which to XOR the first element in 'in'
     * @return the next column in the key scheduling.
     */

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
        // Set number of keys we need - 10 keys x 4 bytes + initial
        int binkeysize = key.length() * 4;

        int colsize = binkeysize + 48 - (32 * ((binkeysize / 64) - 2));
        int keySize = colsize/4;
        int nk = binkeysize/32;

        // Init rcon pointer
        int rconIndex = 1;
        // Init key variable
        int[][] expandedKey = new int[4][keySize];
        // First parse key into 4x4 matrix
        for (int i = 0; i < nk; i++) {
            for (int j = 0; j < 4; j++) {
                expandedKey[j][i] = Integer.parseInt(key.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
            }
        }

        // Set start point - given we have already filled the first key
        int current = nk;
        // Set some temp variables
        int[] a = new int[4];
        int b;
        while (current < keySize) {
            if (current % nk == 0) {
                // We need to go through the g function
                // First copy last word
                for (b = 0; b < 4; b++) {
                    a[b] = expandedKey[b][current - 1];
                }
                // Go through g
                a = schedule_core(a, rconIndex++);
                // XOR with [i-4] word
                for (b = 0; b < 4; b++) {
                    expandedKey[b][current] = a[b] ^ expandedKey[b][current - nk];
                }
            } else if(current % nk ==4 && nk==8){
                for (b = 0; b < 4; b++) {
                    int hex = expandedKey[b][current - 1];
                    expandedKey[b][current] = Constants.sbox[hex / 16][hex % 16] ^ expandedKey[b][current - nk];
                }
            }
            else {
                // Simply XOR with [i-4]
                for (b = 0; b < 4; b++) {
                    expandedKey[b][current] = expandedKey[b][current - 1] ^ expandedKey[b][current - nk];
                }
            }
            current++;
        }

        return expandedKey;
    }

    /**
     * Helper function used in key expansion
     *
     * For each 4th word in the expanded key
     * This function rotates the word
     * Subs with Sbox
     * And XORS each byte with an rcon constant
     */


   /* public void printState(int[][] state) {
        for (int i = 0; i < state.length; i++) {

            for (int j = 0; j < state[i].length; j++) {
                System.out.printf("%02X ", state[i][j]); // In giá trị của mỗi byte, với định dạng Hexadecimal
            }
            System.out.println(); // Xuống dòng sau khi in xong mỗi hàng
        }
    }*/






    /**
     * Converts integer array state to a string
     */
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



    /**
     * Abstract class for encryption
     * To be implemented on an Encryption mode basis
     */
    public abstract String encrypt(String input, String iv, String key);

    /**
     * Abstract class for decryption
     * To be implemented on a Decryption mode basis
     */
    public abstract String decrypt(String input, String iv, String key);


}
