/* 
 * Item means one value of bias with its locations.
 * Location is stored as two arrays of input and output sums (from linear aproximation table).
 */
package linearcryptanalysis;

import static java.lang.Math.abs;
import java.util.ArrayList;

/**
 *
 * @author Sarka Hatasova
 */
public class Item implements Comparable<Item> {

    private final int bias;
    private ArrayList<Integer> appearanceI;
    private ArrayList<Integer> appearanceO;

    public Item(int bias) {
        this.bias = bias;
        appearanceI = new ArrayList<>();
        appearanceO = new ArrayList<>();
    }

    public Item(int bias, int input, int output) {
        this.bias = bias;
        appearanceI = new ArrayList<>();
        appearanceO = new ArrayList<>();

        appearanceI.add(input);
        appearanceO.add(output);
    }

    public void addAppearance(int input, int output) {
        appearanceI.add(input);
        appearanceO.add(output);
    }

    public void printItem() {
        System.out.println("Bias = " + bias + " has " + appearanceI.size() + " appearances.");
    }

    //for access from class SBoxAnalysis
    public void printIOSums() {
        // System.out.println("Appearance of bias " + this.bias);
        String printStr = "{";
        for (int i = 0; i < this.appearanceI.size(); i++) {
            printStr += "{" + Integer.toHexString(this.appearanceI.get(i)) + "," + Integer.toHexString(this.appearanceO.get(i)) + "}, ";
        }
        printStr += "}";
        System.out.println(this.bias + " = " + printStr);
    }

    public void printSizeOfLists() {
        System.out.println("Lists size: " + this.appearanceI.size());
    }

    public void printAppearanceAsArray() {
        String printStr = "{";
        for (int i = 0; i < this.appearanceI.size(); i++) {
            printStr += "{"+Integer.toHexString(appearanceI.get(i)) + "," + Integer.toHexString(appearanceO.get(i)) + "}, ";
        }
        printStr += "}";
        System.out.println(this.bias + " = " + printStr);
    }

    public int getSizeOfInputSums() {
        return this.appearanceI.size();
    }

    public int getInputAtIndex(int i) {
        return this.appearanceI.get(i);
    }

    public int getOutputAtIndex(int i) {
        return this.appearanceO.get(i);
    }

    public int getBias() {
        return bias;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.bias;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        if (this.bias != other.bias) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Item o) {
        // return Integer.compare(this.bias, o.bias); //from lower to higher
        if (abs(this.bias) > abs(o.bias)) {
            return -1;
        }
        if (abs(this.bias) < abs(o.bias)) {
            return 1;
        } else {
            return 0;
        }
    }

}
