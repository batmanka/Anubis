/*
 * This class produces linear aproximation table of 8-bit original S-box
 */
package linearcryptanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import mybabyanubis.Anubis;
import static mybabyanubis.Anubis.sbox;

/**
 *
 * @author Sarka Hatasova
 */
public class SBoxAnalysis8bits {

    private int[][] table;
    private static final int size = 256;
    private ArrayList<Integer> listOfX; //list of indexes, which input Xs are used
    private ArrayList<Integer> listOfY; //list of indexes, which output Ys are used
    private int counter; //counts rows with the same values of input/output
    private ArrayList<Item> listOfAppearances;

    public SBoxAnalysis8bits() {
        table = new int[size][size];
    }

    public static void printIO() {
        System.out.println("\n\nPrinting I/O into S-BOX\n");
        for (int i = 0; i < Anubis.sbox.length; i++) {
            System.out.println(
                    "input: " + Integer.toHexString(i)
                    + " ("
                    + Integer.toBinaryString(i)
                    + "), output: " + Integer.toHexString(Anubis.sbox[i])
                    + " (" + Integer.toBinaryString(Anubis.sbox[i])
                    + ")");
        }
    }

    //input - value to s-box, returns i-bits of s-box output
    //index = index of output of value from s-box, index is from 0(lsb) to 7(msb) 
    public static boolean getY(int value, int index) {
        int result = (sbox[value] >>> index) & 1;

        return result == 1;
    }

    public static boolean getX(int value, int index) {
        int result = (value >>> index) & 1;

        return result == 1;
    }

    private void createLists(int x, int y) {
        listOfX.clear();
        listOfY.clear();

        for (int i = 0; i < 8; i++) {
            if (getX(x, i)) {
                listOfX.add(i);
            }
            if (getX(y, i)) {
                listOfY.add(i);
            }
        }
    }

    private void count(int input, int output) {
        counter = 0;
        int X, Y;
        boolean xorX;
        boolean xorY;

        for (int i = 0; i < size; i++) {
            X = i; //input
            Y = sbox[X]; //output
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
        table[input][output] = counter - (size / 2);
    }

    private void prepareTable() {
        table[0][0] = (size / 2);
        for (int i = 1; i < table.length; i++) {
            table[0][i] = 0;
        }
    }

    public void analyzeSBox() {
        int x, y;
        listOfX = new ArrayList<>();
        listOfY = new ArrayList<>();

        prepareTable();

        for (int i = 1; i < size; i++) {
            x = i; //combination of input bits                     
            for (int j = 1; j < size; j++) {
                y = j; //combination of output bits                            
                createLists(x, y);
                count(x, y);
            }
        }
        countAppearance();
    }

    //this function counts appearances of biases in big table[256][256] - just for better print of results
    private void countAppearance() {
        listOfAppearances = new ArrayList<>();

        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table.length; j++) {
                Item tempItem = new Item(table[i][j]);
                if (listOfAppearances.contains(tempItem)) {//item already exists
                    int index = listOfAppearances.indexOf(tempItem);
                    listOfAppearances.get(index).addAppearance(i, j);
                } else {
                    Item tempItem2 = new Item(table[i][j], i, j);
                    listOfAppearances.add(tempItem2);
                }
            }
        }
        Collections.sort(listOfAppearances);
    }

    public void printBiases() {
        listOfAppearances.stream().forEach((listOfAppearance) -> {
            listOfAppearance.printItem();
        });
    }

    //prints I/O sums of bias
    public void printIOSumsOfBias(int biasForPrint) {

        Item tempItem = new Item(biasForPrint);
        if (listOfAppearances.contains(tempItem)) {
            int index = listOfAppearances.indexOf(tempItem);
            listOfAppearances.get(index).printIOSums();
        } else {
            System.out.println("Bias = " + biasForPrint + " doesn't exists.");
        }
    }

    public void printTable() {
        System.out.println("Table");
        for (int[] table1 : table) {
            for (int j = 0; j < table1.length; j++) {
                System.out.print(Integer.toString(table1[j]) + " ");
            }
            System.out.print("\n");
        }

    }

    //important for speed up of analysis of 16bits I/O SBox
    public int[] getInputSumsAsArray(int bias[]) {
        int[] array;
        int lenghtOfArray;
        int listSize;
        ArrayList<Integer> indexes = new ArrayList();
        int value;

        for (int i = 0; i < bias.length; i++) {
            listSize = listOfAppearances.get(i).getSizeOfInputSums();
            for (int j = 0; j < listSize; j++) {
                value = listOfAppearances.get(i).getInputAtIndex(j);
                if (indexes.contains(value)) {
                    continue;
                }
                indexes.add(value);
            }
        }

        lenghtOfArray = indexes.size();
        array = new int[lenghtOfArray];

        for (int i = 0; i < indexes.size(); i++) {
            array[i] = indexes.get(i);
        }
        Arrays.sort(array);

        return array;
    }

}
