/* This class counts one value from Linear Aproximation Table of 16-bits Large SBOX*/
package linearcryptanalysis;

import java.util.ArrayList;

/**
 *
 * @author Sarka Hatasova
 */
public class LargeSBoxAnalysis {

    private final int size = 65536;
    private int counter; //counts rows with the same values of input/output
    private LargeSBox l;
    private ArrayList<Integer> listOfX; //list of indexes, which input Xs are used
    private ArrayList<Integer> listOfY; //list of indexes, which output Ys are used

    public LargeSBoxAnalysis(LargeSBox l) {
        this.l = l;
        listOfX = new ArrayList<>();
        listOfY = new ArrayList<>();
    }

    public static boolean getX(int value, int index) {
        int result = (value >>> index) & 1;
        return result == 1;
    }

    private void createLists(int x, int y) {
        listOfX.clear();
        listOfY.clear();

        for (int i = 0; i < 16; i++) {
            if (getX(x, i)) {
                listOfX.add(i);
            }
            if (getX(y, i)) {
                listOfY.add(i);
            }
        }
    }

    private int count() {
        counter = 0;
        int X, Y;
        boolean xorX;
        boolean xorY;

        for (int i = 0; i < size; i++) {
            X = i; //input
            Y = l.largeSBox[X]; //output
            xorX = false;
            xorY = false;

            for (Integer listOfX1 : listOfX) {
                xorX = xorX ^ getX(X, listOfX1);
            }

            for (Integer listOfY1 : listOfY) {
                xorY = xorY ^ getX(Y, listOfY1);
            }
            if (xorX == xorY) {
                counter++;
            }
        }
        return (counter - (size / 2));
    }

    public int analyzeOneValueAtPosition(int inputSum, int outputSum) {
        int value;
        createLists(inputSum, outputSum);
        value = count();
        //System.out.println("[" + inputSum + ", " + outputSum + "] = " + value);
        return value;
    }

}
