/*
 * Implementation of Baby Anubis cipher exactly according to the chapter 4 in my master thesis
 */
package mybabyanubis;

import static java.lang.System.exit;
import java.util.ArrayList;

/**
 *
 * @author Sarka Hatasova
 */
public class Anubis {

    public static final int sbox[] = {
        0xa7, 0xd3, 0xe6, 0x71, 0xd0, 0xac, 0x4d, 0x79,
        0x3a, 0xc9, 0x91, 0xfc, 0x1e, 0x47, 0x54, 0xbd,
        0x8c, 0xa5, 0x7a, 0xfb, 0x63, 0xb8, 0xdd, 0xd4,
        0xe5, 0xb3, 0xc5, 0xbe, 0xa9, 0x88, 0x0c, 0xa2,
        0x39, 0xdf, 0x29, 0xda, 0x2b, 0xa8, 0xcb, 0x4c,
        0x4b, 0x22, 0xaa, 0x24, 0x41, 0x70, 0xa6, 0xf9,
        0x5a, 0xe2, 0xb0, 0x36, 0x7d, 0xe4, 0x33, 0xff,
        0x60, 0x20, 0x08, 0x8b, 0x5e, 0xab, 0x7f, 0x78,
        0x7c, 0x2c, 0x57, 0xd2, 0xdc, 0x6d, 0x7e, 0x0d,
        0x53, 0x94, 0xc3, 0x28, 0x27, 0x06, 0x5f, 0xad,
        0x67, 0x5c, 0x55, 0x48, 0x0e, 0x52, 0xea, 0x42,
        0x5b, 0x5d, 0x30, 0x58, 0x51, 0x59, 0x3c, 0x4e,
        0x38, 0x8a, 0x72, 0x14, 0xe7, 0xc6, 0xde, 0x50,
        0x8e, 0x92, 0xd1, 0x77, 0x93, 0x45, 0x9a, 0xce,
        0x2d, 0x03, 0x62, 0xb6, 0xb9, 0xbf, 0x96, 0x6b,
        0x3f, 0x07, 0x12, 0xae, 0x40, 0x34, 0x46, 0x3e,
        0xdb, 0xcf, 0xec, 0xcc, 0xc1, 0xa1, 0xc0, 0xd6,
        0x1d, 0xf4, 0x61, 0x3b, 0x10, 0xd8, 0x68, 0xa0,
        0xb1, 0x0a, 0x69, 0x6c, 0x49, 0xfa, 0x76, 0xc4,
        0x9e, 0x9b, 0x6e, 0x99, 0xc2, 0xb7, 0x98, 0xbc,
        0x8f, 0x85, 0x1f, 0xb4, 0xf8, 0x11, 0x2e, 0x00,
        0x25, 0x1c, 0x2a, 0x3d, 0x05, 0x4f, 0x7b, 0xb2,
        0x32, 0x90, 0xaf, 0x19, 0xa3, 0xf7, 0x73, 0x9d,
        0x15, 0x74, 0xee, 0xca, 0x9f, 0x0f, 0x1b, 0x75,
        0x86, 0x84, 0x9c, 0x4a, 0x97, 0x1a, 0x65, 0xf6,
        0xed, 0x09, 0xbb, 0x26, 0x83, 0xeb, 0x6f, 0x81,
        0x04, 0x6a, 0x43, 0x01, 0x17, 0xe1, 0x87, 0xf5,
        0x8d, 0xe3, 0x23, 0x80, 0x44, 0x16, 0x66, 0x21,
        0xfe, 0xd5, 0x31, 0xd9, 0x35, 0x18, 0x02, 0x64,
        0xf2, 0xf1, 0x56, 0xcd, 0x82, 0xc8, 0xba, 0xf0,
        0xef, 0xe9, 0xe8, 0xfd, 0x89, 0xd7, 0xc7, 0xb5,
        0xa4, 0x2f, 0x95, 0x13, 0x0b, 0xf3, 0xe0, 0x37
    };

    private static final int hadamardMatrix[][] = { //elements are from GF(2^8)
        {2, 3},
        {3, 2}
    };

    private static final int vandermondeMatrix[][] = { //elements are from GF(2^8)
        {1, 1},
        {1, 2}
    };

    private static final int irrPolynomialInt = 285; //irreducible polynomial of GF(2^8): x^8+x^4+x^3+x^2+1
    private static final String irrPolynomialString = "100011101"; //the same irreducible polynomial but in another notation

    /*16-bytes input and output*/
    private final int numOfBytes = 4;
    private int numOfRounds = 12;
    private int numOfFullRounds;
    private int[] plainText;
    private int[] key;
    private int m = 2; //rows and cols in matrix
    private ArrayList<Matrix> kappa;
    private ArrayList<Matrix> keys; //keys for encrypt
    private ArrayList<Matrix> reversedKeys; //keys for decrypt
    private Matrix h; //Hadamard matrix - used in linear diffusion layer (part of key evolution and round function)
    private Matrix v; //Vandermonde matrix - used in key extraction (part of key selection)
    private Matrix input; //plain text in matrix form
    private boolean encOrDec; //flag for key addition - true -> use normal key schedule (encrypt)
    //false -> use reversed key schedule (decrypt)

    //global variable for statistics purposes only
    private int roundsOfKeys;

    public Anubis(int[] plainText, int[] key) {
        if (plainText.length != numOfBytes || key.length != numOfBytes) {
            System.out.println("Wrong count of bytes.");
            return;
        }
        this.plainText = new int[numOfBytes];
        System.arraycopy(plainText, 0, this.plainText, 0, plainText.length);

        this.key = new int[numOfBytes];
        System.arraycopy(key, 0, this.key, 0, key.length);

        this.h = new Matrix(hadamardMatrix, m, "Hadamard matrix");
        this.v = new Matrix(vandermondeMatrix, m, "Vandermonde matrix");

        int[][] inputPT = {
            {plainText[0], plainText[1]},
            {plainText[2], plainText[3]}
        };

        this.numOfFullRounds = numOfRounds - 1; //11
        this.input = new Matrix(inputPT, m, "Plain Text -> Cipher Text");
    }

    //reduced rounds constructor, numOfFullRounds is int number in <1,11>
    public Anubis(int[] plainText, int[] key, int numOfFullRounds) {
        if (plainText.length != numOfBytes || key.length != numOfBytes) {
            System.out.println("Wrong count of bytes.");
            return;
        }
        this.plainText = new int[numOfBytes];
        System.arraycopy(plainText, 0, this.plainText, 0, plainText.length);

        this.key = new int[numOfBytes];
        System.arraycopy(key, 0, this.key, 0, key.length);

        this.h = new Matrix(hadamardMatrix, m, "Hadamard matrix");
        this.v = new Matrix(vandermondeMatrix, m, "Vandermonde matrix");

        int[][] inputPT = {
            {plainText[0], plainText[1]},
            {plainText[2], plainText[3]}
        };
        this.numOfFullRounds = numOfFullRounds;
        this.input = new Matrix(inputPT, m, "Plain Text -> Cipher Text");
    }

    //round function - xor key with data block
    private void keyAddition(int index) { //index = number of round -> index of key  

        if (encOrDec) { //encrypt
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    input.matrix[i][j] = input.matrix[i][j] ^ keys.get(index).matrix[i][j];
                }
            }
        } else { //decrypt - use reverse key schedule
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    input.matrix[i][j] = input.matrix[i][j] ^ reversedKeys.get(index).matrix[i][j];
                }
            }
        }
    }

    private void keyAdditionReduced(int index) { //index = number of round -> index of key  
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                input.matrix[i][j] = input.matrix[i][j] ^ keys.get(index).matrix[i][j];
            }
        }
    }

    private void keyAdditionReducedReverse(int index) { //index = number of round -> index of key  
        int reversedIndex = numOfRounds - (numOfFullRounds - index) - 1;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                input.matrix[i][j] = input.matrix[i][j] ^ reversedKeys.get(reversedIndex).matrix[i][j];
            }
        }
    }

    //round function - s-box
    private void nonlinearLayer() {
        for (int row = 0; row < m; row++) {
            for (int col = 0; col < m; col++) {
                input.matrix[row][col] = sbox[input.matrix[row][col]];
            }
        }
    }

    //round function - transpose matrix
    private void transposition() {
        input.transposeItself(input.matrix);
    }

    //round function - multiplication with Hadamard matrix
    private void linearDiffusionLayer() {
        input.matrix = groupMultiplicationH(input.matrix);
    }

    //modified round function for LargeSBoxAnalysis - multiplicate input matrix with Hadamard matrix
    public int[][] linearDiffusionLayer(int[][] matrix) {
        return groupMultiplicationH(matrix);
    }

    private void firstRound() {
        keyAddition(0);
    }

    private void rounds() {
        for (int i = 1; i < numOfRounds; i++) {
            transposition();//change order
            nonlinearLayer();
            
            linearDiffusionLayer();
            keyAddition(i);
        }
    }

    private void lastRound() {
        nonlinearLayer();
        transposition();
        keyAddition(numOfRounds);
    }

    //for reduced version
    private void firstRound(int indexOfKey) {
        keyAdditionReduced(indexOfKey);
    }

    private void rounds(int fullRounds) {
        for (int i = 1; i < fullRounds + 1; i++) {
            transposition();
            nonlinearLayer();//change order
            
            linearDiffusionLayer();
            if (encOrDec) {
                keyAddition(i);
            } else {
                keyAdditionReducedReverse(i);
            }
        }
    }

    private void lastRound(int indexOfKey) {
       
        transposition();
         nonlinearLayer();//change order
        keyAdditionReduced(indexOfKey);
    }

    private void crypt(boolean flag) { //flag == 1 -> encypt, flag == 0 -> decrypt
        encOrDec = flag;
        if (numOfFullRounds + 1 == numOfRounds) { //standard num of rounds
            firstRound();
            rounds();
            lastRound();
            return;
        } else if (encOrDec) { //variable num of rounds, encription
            firstRound(); //key 0
            rounds(numOfFullRounds); // key 1... key X
            lastRound(numOfFullRounds + 1); //key X+1//
        } else {//variable num of rounds, decription
            firstRound(numOfFullRounds + 1);
            rounds(numOfFullRounds);
            lastRound(0);
        }
    }

    public void encrypt() {
        crypt(true);
    }

    public void decrypt() {
        crypt(false);
    }

    private void keyEvolution() {
        /* kappa0 - kappa11 -> 12 matrices + key selection -> create round keys K0 - K11 (represents in matrix too) */
        kappa = new ArrayList<>();

        int[][] temp = {
            {key[0], key[1]},
            {key[2], key[3]}
        };

        Matrix matrix = new Matrix(temp, m, "kappa0");
        kappa.add(matrix); //kappa0 is ready and stored

        //key evolution: prepare other kappas (kappa1-kappa11), every element of kappa send to s-box  
        for (int i = 1; i < numOfRounds + 1; i++) {

            for (int row = 0; row < m; row++) {
                for (int col = 0; col < m; col++) {
                    temp[row][col] = sbox[temp[row][col]];
                }
            }

            //permutation pi
            int[][] temp2 = new int[m][m];

            for (int j = 0; j < temp2.length; j++) {
                for (int k = 0; k < temp2.length; k++) {
                    temp2[j][k] = temp[Math.floorMod((j - k), m)][k];
                }
            }

            //difusion layer - group multiplication in GF(2^8) is used here, not "ordinary" multiplication
            String name = "kappa" + Integer.toString(i);
            Matrix mat = new Matrix(groupMultiplicationH(temp2), m, name);//

            //compute round constant
            int[][] roundConst = new int[m][m];

            for (int row = 0; row < roundConst.length; row++) {
                for (int col = 0; col < roundConst.length; col++) {
                    if (row == 0) {
                        roundConst[row][col] = sbox[4 * (i - 1) + col]; //4
                    } else {
                        roundConst[row][col] = 0;
                    }
                }
            }

            //key addition - XOR round constant with Matrix mat from difusion layer
            for (int row = 0; row < roundConst.length; row++) {
                for (int col = 0; col < roundConst.length; col++) {
                    mat.matrix[row][col] = mat.matrix[row][col] ^ roundConst[row][col];
                }
            }

            kappa.add(mat);
            //copy this kappa to next iteration
            for (int j = 0; j < m; j++) {
                System.arraycopy(mat.matrix[j], 0, temp[j], 0, m);
            }
        }//end key evolution

    }

    private void keySelection() {
        keys = new ArrayList<>();

        for (int i = 0; i < kappa.size(); i++) {

            String name = "key" + Integer.toString(i);
            Matrix matrix = new Matrix(kappa.get(i).matrix, m, name); //keys are made from kappas

            //send each key0-11 to s-box
            for (int row = 0; row < m; row++) {
                for (int col = 0; col < m; col++) {
                    matrix.matrix[row][col] = sbox[matrix.matrix[row][col]];
                }
            }

            matrix.matrix = groupMultiplicationV(matrix.matrix);//key extraction - b = V * a                                 
            matrix.transposeItself(matrix.matrix); //transposition            
            keys.add(matrix); //key is ready and stored 
        }
    }//end key selection

    private void prepareReversedKeys() {
        //generate inverse key schedule: K'^0 = K^R, K'^R = K^0, K'^r = theta(K^{R-r}):
        reversedKeys = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            if (i == 0 || i == keys.size() - 1) { //K'^0 = K^R, K'^R = K^0
                reversedKeys.add(keys.get(keys.size() - 1 - i));
            } else { //K'^r = theta(K^{R-r}):
                String name = "reversed key" + Integer.toString(i);
                Matrix matrix = new Matrix(groupMultiplicationH(keys.get(keys.size() - 1 - i).matrix), m, name);
                reversedKeys.add(matrix);
            }
        }
    }

    public void keySchedule() {
        keyEvolution();
        keySelection();
        prepareReversedKeys();
//        for (int i = 0; i < keys.size(); i++) {
//            keys.get(i).printHex();
//            
//        }
    }

    public int multiplicateTwoNumbers(int a, int b) { //multiplication two numbers from GF(2^8)
        if (a < 0 || b < 0) {
            System.out.println("Bad numbers.");
            exit(-1);
        }

        if (a == 0 || b == 0) {
            return 0;
        }
        if (a == 1) {
            return b;
        }
        if (b == 1) {
            return a;
        }

        String bBinary;
        bBinary = Integer.toBinaryString(b);
        int countOfXors = bBinary.length() - 1;
        int temp = a << countOfXors;
        int temp2;

        for (int i = 1; i < countOfXors + 1; i++) {
            if (bBinary.charAt(i) == '0') {
                continue;
            }
            temp2 = a << countOfXors - i;
            temp = temp ^ temp2;
        }
        return modulo(temp);
    }

    public int modulo(int x) { //use irreducible polynomial to reduct input 
        String xBinary = Integer.toBinaryString(x);

        if (xBinary.length() < irrPolynomialString.length()) {
            return x;
        }

        if (x == irrPolynomialInt) {
            return 0;
        }

        int countBitsOfIrr = irrPolynomialString.length();
        int countBitsOfNumber = xBinary.length();
        int tempIrr = irrPolynomialInt << (countBitsOfNumber - countBitsOfIrr);
        int result = 0;
        int order = 256; //num of elements in group - 2^8

        while (true) {
            for (int i = 0; i < (countBitsOfNumber - countBitsOfIrr) + 1; i++) {
                if (i == 0) {
                    result = x ^ tempIrr;
                } else {
                    try {
                        if (Integer.toBinaryString(result).length() > i + 1 && '1' == irrPolynomialString.charAt(i)) {//bit on i possition of irr is 1
                            tempIrr = tempIrr >>> 1;
                            result = result ^ tempIrr;
                        } else {//bit on i possition of irr is 0
                            tempIrr = tempIrr >>> 1;
                        }
                    } catch (Exception e) { //maybe no need here :-)
                        System.out.println("exception");
                        return result;
                    }
                }

            }//end for

            if (result < order + 1) {
                break;
            }
            if (result == irrPolynomialInt) {
                return 0;
            }
            x = result;
            countBitsOfNumber = Integer.toBinaryString(x).length();
            tempIrr = irrPolynomialInt << (countBitsOfNumber - countBitsOfIrr);
            if ((countBitsOfNumber - countBitsOfIrr) < 0) {
                break;
            }

        }//end while
        return result;
    }

    private int[][] groupMultiplicationH(int[][] matrix) { //this function multiplicate input matrix with hadamard matrix (from right)
        int size = matrix.length;

        int[][] C = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    C[i][j] = addTwoNumbers(C[i][j], multiplicateTwoNumbers(matrix[i][k], h.matrix[k][j]));
                    C[i][j] = modulo(C[i][j]);
                }
            }
        }
        return C;
    }

    private int addTwoNumbers(int a, int b) {
        return a ^ b;
    }

    private int[][] groupMultiplicationV(int[][] matrix) { //this function multiplicate input matrix with vandermonde matrix (from left)
        int size = matrix.length;
        int plusValue;

        int[][] C = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    plusValue = multiplicateTwoNumbers(v.matrix[i][k], matrix[k][j]);
                    C[i][j] = addTwoNumbers(C[i][j], plusValue);
                    C[i][j] = modulo(C[i][j]);
                }
            }
        }
        return C;
    }

    //print output in hex
    public void printOutputText() {
        String ct = "";

        for (int[] matrix1 : input.matrix) {
            for (int j = 0; j < input.matrix.length; j++) {
                if (matrix1[j] < 16) {
                    ct += "0" + (Integer.toHexString(matrix1[j]));
                } else {
                    ct += (Integer.toHexString(matrix1[j]));
                }
            }
        }
        if (encOrDec) {
            System.out.println("CT (hex): " + ct);
        } else {
            System.out.println("OT (hex): " + ct);
        }

    }

    //print output in int
    public void printOutputTextInt() {
        String ct = "";

        for (int[] matrix1 : input.matrix) {
            for (int j = 0; j < input.matrix.length; j++) {
                ct += "," + (Integer.toString(matrix1[j]));
            }
        }
        if (encOrDec) {
            System.out.println("CT (int): " + ct);
        } else {
            System.out.println("OT (int): " + ct);
        }
    }

    //print input in hex
    public void printInputText() {
        String ot = "";
        for (int j = 0; j < plainText.length; j++) {
            if (plainText[j] < 16) {
                ot += "0" + (Integer.toHexString(plainText[j]));
            } else {
                ot += (Integer.toHexString(plainText[j]));
            }
        }
        if (encOrDec) {
            System.out.println("OT (hex): " + ot);
        } else {
            System.out.println("CT (hex): " + ot);
        }
    }

    //print input in int
    public void printInputTextInt() {
        String ot = "";
        for (int j = 0; j < plainText.length; j++) {
            ot += (Integer.toString(plainText[j]));
        }
        if (encOrDec) {
            System.out.println("OT (int): " + ot);
        } else {
            System.out.println("CT (int): " + ot);
        }
    }

    public void print() {
        System.out.println("Anubis cipher");
        System.out.println("\nPlain Text:");
        for (int i = 0; i < plainText.length; i++) {
            System.out.print(" " + plainText[i] + " ");
        }
        System.out.println();
    }

    public byte[] outputAsByteArray() {
        byte outputArray[] = new byte[numOfBytes];
        outputArray[0] = (byte) input.matrix[0][0];
        outputArray[1] = (byte) input.matrix[0][1];
        outputArray[2] = (byte) input.matrix[1][0];
        outputArray[3] = (byte) input.matrix[1][1];
        return outputArray;
    }

    public int[] outputAsIntArray() {
        int outputArray[] = new int[numOfBytes];
        outputArray[0] = input.matrix[0][0];
        outputArray[1] = input.matrix[0][1];
        outputArray[2] = input.matrix[1][0];
        outputArray[3] = input.matrix[1][1];
        return outputArray;
    }

    //for cryptanalysis
    public void printLastRoundKey(int fullRounds) {
        keys.get(fullRounds + 1).printHex();
    }

    public int getPartOfRoundKey(int fullRounds) {
        int partKey = keys.get(fullRounds + 1).matrix[0][0];
        partKey = partKey << 8;
        partKey = partKey ^ keys.get(fullRounds + 1).matrix[1][0];
        return partKey;
    }

    public int getPartOfCipherKey() {
        int partKey = key[0];
        partKey = partKey << 8;
        partKey = partKey ^ key[2];
        return partKey;
    }

    //for two full rounds analysis
    public int getPartOfRoundKey2(int fullRounds) {
        int partKey = keys.get(fullRounds + 1).matrix[0][1];
        partKey = partKey << 8;
        partKey = partKey ^ keys.get(fullRounds + 1).matrix[1][1];
        return partKey;
    }

    public int getPartOfCipherKey2() {
        int partKey = key[1];
        partKey = partKey << 8;
        partKey = partKey ^ key[3];
        return partKey;
    }

    /* All functions below are made for statistical purposes only
     * They are here cause I'm so lazy to make so many getters for all needed private variables :-)
     */
    private void keySelectionForStat() {
        keys = new ArrayList<>();

        for (int i = 0; i < kappa.size(); i++) {

            String name = "key" + Integer.toString(i);
            Matrix matrix = new Matrix(kappa.get(i).matrix, m, name); //keys are made from kappas

            //send each key0-11 to s-box
            for (int row = 0; row < m; row++) {
                for (int col = 0; col < m; col++) {
                    matrix.matrix[row][col] = sbox[matrix.matrix[row][col]];
                }
            }

            matrix.matrix = groupMultiplicationV(matrix.matrix);//key extraction - b = V * a                                 
            matrix.transposeItself(matrix.matrix); //transposition            
            keys.add(matrix); //key is ready and stored 
        }//end for cycle of key evolution
    }

    private void keyEvolutionForStat() {
        kappa = new ArrayList<>();

        int[][] temp = {
            {key[0], key[1]},
            {key[2], key[3]}
        };

        Matrix matrix = new Matrix(temp, m, "kappa0");
        kappa.add(matrix); //kappa0 is ready and stored

        //key evolution: prepare other kappas (kappa1-kappa11), every element of kappa send to s-box  
        for (int i = 1; i < roundsOfKeys; i++) {
            int sboxConst = i % 12 + 1;

            for (int row = 0; row < m; row++) {
                for (int col = 0; col < m; col++) {
                    temp[row][col] = sbox[temp[row][col]];
                }
            }

            //permutation pi
            int[][] temp2 = new int[m][m];

            for (int j = 0; j < temp2.length; j++) {
                for (int k = 0; k < temp2.length; k++) {
                    temp2[j][k] = temp[Math.floorMod((j - k), m)][k];
                }
            }

            //difusion layer - group multiplication in GF(2^8) is used here, not "ordinary" multiplication
            String name = "kappa" + Integer.toString(i);
            Matrix mat = new Matrix(groupMultiplicationH(temp2), m, name);//

            //compute round constant
            int[][] roundConst = new int[m][m];

            for (int row = 0; row < roundConst.length; row++) {
                for (int col = 0; col < roundConst.length; col++) {
                    if (row == 0) {
                        roundConst[row][col] = sbox[4 * (sboxConst - 1) + col]; //4
                    } else {
                        roundConst[row][col] = 0;
                    }
                }
            }

            //key addition - XOR round constant with Matrix mat from difusion layer
            for (int row = 0; row < roundConst.length; row++) {
                for (int col = 0; col < roundConst.length; col++) {
                    mat.matrix[row][col] = mat.matrix[row][col] ^ roundConst[row][col];
                }
            }

            kappa.add(mat);
            //copy this kappa to next iteration
            for (int j = 0; j < m; j++) {
                System.arraycopy(mat.matrix[j], 0, temp[j], 0, m);
            }

        }//end key evolution
    }

    //output of this function -> keys for 100 (default value) rounds = 100*4 bytes -> 400 bytes 
    public byte[] get100KesInBytes(int rounds) {
        this.roundsOfKeys = rounds;
        keyEvolutionForStat();
        keySelectionForStat();
        //output is in keys array list of matrices

        byte outputArray[] = new byte[roundsOfKeys * 4];
        int index = 0;
        for (int i = 0; i < roundsOfKeys; i++) {
            outputArray[index] = (byte) keys.get(i).matrix[0][0];
            outputArray[index + 1] = (byte) keys.get(i).matrix[0][1];
            outputArray[index + 2] = (byte) keys.get(i).matrix[1][0];
            outputArray[index + 3] = (byte) keys.get(i).matrix[1][1];
            index += 4;
        }
        return outputArray;
    }

}
