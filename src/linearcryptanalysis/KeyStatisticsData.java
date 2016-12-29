/* 
 * Support class for storing keys during statistical evaluation of attack
 */
package linearcryptanalysis;

/**
 *
 * @author Sarka Hatasova
 */

public class KeyStatisticsData implements Comparable<KeyStatisticsData>{
    private final int cipherKey;
    private final int roundKeyToGuess;
    private int positionOfRoundKey;

    public KeyStatisticsData(int cipherKey, int roundKeyToGuess) {
        this.cipherKey = cipherKey;
        this.roundKeyToGuess = roundKeyToGuess;
    }

    public int getPositionOfRoundKey() {
        return positionOfRoundKey;
    }

    public void setPositionOfRoundKey(int positionOfRoundKey) {
        this.positionOfRoundKey = positionOfRoundKey;
    }

    public int getRoundKeyToGuess() {
        return roundKeyToGuess;
    }
      
    public void print(){    
        System.out.println("CT: "+cipherKey+", roundKeyToGuess: "+Integer.toHexString(roundKeyToGuess)+", atPosition: "+positionOfRoundKey);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.roundKeyToGuess;
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
        final KeyStatisticsData other = (KeyStatisticsData) obj;
        if (this.positionOfRoundKey != other.positionOfRoundKey) {
            return false;
        }
        return true;
    }
  
    @Override
    public int compareTo(KeyStatisticsData o) {
        if (this.positionOfRoundKey > o.positionOfRoundKey) {
            return 1;
        }
        if (this.positionOfRoundKey < o.positionOfRoundKey) {
            return -1;
        } else {
            return 0;
        }
    }
    
}
