/* This class simulates 16 bits OUTPUT/INPUT large SBOX 
 * = two original 8-bits sboxes + half of linear diffusion layer
 */
package linearcryptanalysis;

import mybabyanubis.Anubis;
import mybabyanubis.Matrix;

/**
 *
 * @author Sarka Hatasova
 */
public class LargeSBox {

    public int[] largeSBox;
    private final int size = 65536;
    int[][] tempMatrix;
    Anubis a;

    public LargeSBox() {
        largeSBox = new int[size];
        tempMatrix = new int[2][2];
        int[] dummyArray = {0, 0, 0, 0};
        a = new Anubis(dummyArray, dummyArray);
    }

    public void fillLargeSBox() {
        System.out.println("Preparing Large SBOX...");
        int x, x1, x2; //input of large SBOX
        int y; //output of large SBOX -> filling for array largeSBox[]
        Matrix mat;

        for (int i = 0; i < largeSBox.length; i++) {
            x = i;

            x1 = i >>> 8; //8 msb bits of i
            x2 = x ^ (x1 << 8); //8 lsb bits of i

            //original 8-bit sbox
            x1 = Anubis.sbox[x1];
            x2 = Anubis.sbox[x2];

            //linear diffusion layer
            tempMatrix[0][0] = x1;
            tempMatrix[0][1] = x2;
            tempMatrix[1][0] = 0;
            tempMatrix[1][1] = 0;

            mat = new Matrix(tempMatrix, 2, "");
            mat.matrix = a.linearDiffusionLayer(mat.matrix);

            //result is in first row of 2x2 matrix
            y = mat.matrix[0][0];
            y = y << 8;
            y = y ^ (mat.matrix[0][1]);
            largeSBox[i] = y; //store result
        }
        System.out.println("Large SBOX was created.");
    }

    //Look for input of this large S-box with output value
    public int searchValue(int value) {
        for (int i = 0; i < largeSBox.length; i++) {
            if (largeSBox[i] == value) {
                return i;
            }
        }
        return -1;
    }

}
