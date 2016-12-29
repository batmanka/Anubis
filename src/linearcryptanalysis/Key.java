/* 
 * Support class for storing keys during statistical evaluation of attack
 */
package linearcryptanalysis;

/**
 *
 * @author Sarka Hatasova
 */
public class Key implements Comparable<Key>{
    public final int key;
    private int count; 
    private double bias;

    public Key(int key) {
        this.key = key;
        this.count = 0;
    }
    
    public void add(){
    this.count++;    
    }
    
    public void countBias(int n){
    this.bias = (double) Math.abs(count-(n/2))/n;
    }

    public double getBias() {
        return bias;
    }  

    public int getKey() {
        return key;
    }

      @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Key other = (Key) obj;
        if (this.bias != other.bias) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.bias) ^ (Double.doubleToLongBits(this.bias) >>> 32));
        return hash;
    }
    
    
    @Override
    public int compareTo(Key o) {
          if (this.bias > o.bias) {
            return -1;
        }
        if (this.bias < o.bias) {
            return 1;
        } else {
            return 0;
        }
    }
}
