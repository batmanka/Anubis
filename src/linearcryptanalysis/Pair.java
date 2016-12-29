/* 
 * Support class for storing pairs of (OT, CT) during attack
 */
package linearcryptanalysis;

/**
 *
 * @author Sarka Hatasova
 */

public class Pair {
    public int[] pt;
    public int[] ct;

    public Pair(int[] pt, int[] ct) {
        this.pt = pt;
        this.ct = ct;
    }
    public void print(){
        System.out.println("PT: ["+pt[0]+", "+pt[1]+", "+pt[2]+", "+pt[3]+"], CT: ["+ct[0]+", "+ct[1]+", "+ct[2]+", "+ct[3]+"]");  
    }    
}
