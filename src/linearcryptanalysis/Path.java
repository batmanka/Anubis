/*
 * Support class storing Path through cipher's  Large 16-bits S-boxes
 */
package linearcryptanalysis;

import static java.lang.Math.abs;

/**
 *
 * @author Sarka Hatasova
 */

public class Path implements Comparable<Path> {

    private final int round1Input;
    private final int round2Input;
    private final int round1Output;
    private final int round2Output;
    private final int round1InputB;
    private final int round1OutputB;
    private final double biasOfAllAproximation;
    private final boolean twoActiveSBoxes;

    //contructor for two active S-boxes
    public Path(int round1Input, int round1Output, int round2Input, int round2Output, double biasOfAllAproximation) {
        this.round1Input = round1Input;
        this.round2Input = round2Input;
        this.round1Output = round1Output;
        this.round2Output = round2Output;
        this.biasOfAllAproximation = biasOfAllAproximation;
        this.twoActiveSBoxes = true;
        this.round1InputB = -1;
        this.round1OutputB = -1;
    }

    //contructor for three active S-boxes
    public Path(int round1InputA, int round1OutputA, int round1InputB, int round1OutputB, int round2Input, int round2Output, double biasOfAllAproximation) {
        this.round1Input = round1InputA;
        this.round2Input = round2Input;
        this.round1Output = round1OutputA;
        this.round2Output = round2Output;
        this.round1InputB = round1InputB;
        this.round1OutputB = round1OutputB;
        this.biasOfAllAproximation = biasOfAllAproximation;
        this.twoActiveSBoxes = false;
    }

    public void print() {
        if (twoActiveSBoxes) {
            System.out.println("Two round approximation with " + String.format("%.10f", biasOfAllAproximation));
            System.out.println("1st round: " + Integer.toHexString(round1Input) + " -> " + Integer.toHexString(round1Output));
            System.out.println("2nd round: " + Integer.toHexString(round2Input) + " -> " + Integer.toHexString(round2Output) + "\n");
        } else {
            System.out.println("Two round approximation with " + String.format("%.10f", biasOfAllAproximation));
            System.out.println("1st round: " + Integer.toHexString(round1Input) + " -> " + Integer.toHexString(round1Output)
                    + ", " + Integer.toHexString(round1InputB) + " -> " + Integer.toHexString(round1OutputB));
            System.out.println("2nd round: " + Integer.toHexString(round2Input) + " -> " + Integer.toHexString(round2Output) + "\n");
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + this.round1Input + this.round2Input;
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
        final Path other = (Path) obj;
        if (this.biasOfAllAproximation != other.biasOfAllAproximation) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Path o) {
        // return Integer.compare(this.bias, o.bias); //from lower to higher
        if (abs(this.biasOfAllAproximation) > abs(o.biasOfAllAproximation)) {
            return -1;
        }
        if (abs(this.biasOfAllAproximation) < abs(o.biasOfAllAproximation)) {
            return 1;
        } else {
            return 0;
        }
    }

}
