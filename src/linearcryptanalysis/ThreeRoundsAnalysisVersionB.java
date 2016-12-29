/*
 * Linear cryptanalysis of three rounds of Baby Anubis Cipher
 * =============================================================
 * Three rounds of Baby Anubis contains: 
 * starting key addition, 
 * two full rounds (transpozition + Large S-box (nonlinear+linear diffusion layer)+ key addition),
 * and last round (transpozition + nonlinear layer + key addition)
 */
package linearcryptanalysis;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import mybabyanubis.Anubis;
import static mybabyanubis.Anubis.sbox;

/**
 *
 * @author Sarka Hatasova
 */
public class ThreeRoundsAnalysisVersionB {

    private int[] plainText = {0, 0, 0, 0};
    private int[] cipherText;
    private int[] key = {0, 0, 0, 0};
    private final int numOfPairs;
    private int counter;
    private Anubis anubis;
    private final int rounds = 2; //number of full rounds
    private ArrayList<Key> keyList;
    private ArrayList<Pair> pairList;
    private final int numOfKeys;
    private int numKeysForStat;
    static final int max = 255;
    static final int min = 0;
    private ArrayList<KeyStatisticsData> keyData;
    private KeyStatisticsData tempKeyData;
    private SecureRandom rng;

    public ThreeRoundsAnalysisVersionB(int[] key, int num) {
        this.key = key;
        this.numOfPairs = num;
        this.counter = 0;
        this.numOfKeys = 65536;
        preparePairs();
        System.out.println("Key what we're looking for: ");
        anubis.printLastRoundKey(rounds);
        rng = new SecureRandom();
    }

    public ThreeRoundsAnalysisVersionB(int numKeysForStat, int num) {
        this.numOfPairs = num;
        this.numOfKeys = 65536;
        this.numKeysForStat = numKeysForStat;
        keyData = new ArrayList<>();
        rng = new SecureRandom();
    }

    private void generateRandomKey() {
        for (int i = 0; i < key.length; i++) {
            key[i] = rng.nextInt(max);
        }
    }

    private void countMedian() {
        Collections.sort(keyData);
        int middle = numKeysForStat / 2;
        int median = keyData.get(middle).getPositionOfRoundKey() + 1;
        System.out.println("Median: " + median);
    }

    private void countVariance(double diam) {
        double var = 0;
        for (int i = 0; i < numKeysForStat; i++) {
            var += Math.pow(keyData.get(i).getPositionOfRoundKey() - diam, 2.0);
        }
        var = (double) var / numKeysForStat;
        double sigma = Math.sqrt(var);
        System.out.println("Variance: " + String.format("%.5f", var));
        System.out.println("Probabilistic deviation: " + String.format("%.5f", sigma));
    }

    private void countPositions() {
        int diam = 0;
        double result;
        int best = 0;
        int worst = 0;
        int pos;
        for (int i = 0; i < numKeysForStat; i++) {
            pos = keyData.get(i).getPositionOfRoundKey() + 1;
            diam += pos;
            if (i == 0) {
                best = pos;
                worst = pos;
            } else {
                if (best > pos) {
                    best = pos;
                }
                if (worst < pos) {
                    worst = pos;
                }
            }
        }
        result = (double) diam / numKeysForStat;
        System.out.println("Average position of key: " + String.format("%.5f", result));
        System.out.println("The best position of key: " + best);
        System.out.println("The worst position of key: " + worst);
        countVariance(result-1);
        countMedian();
    }

    public void collectStaticticsData() {
        for (int i = 0; i < numKeysForStat; i++) {
            System.out.println("i: " + i);
            this.counter = 0;
            generateRandomKey();
            preparePairs();

            tempKeyData = new KeyStatisticsData(anubis.getPartOfCipherKey2(), anubis.getPartOfRoundKey2(rounds));
            keyData.add(tempKeyData);
            cryptanalysis();

            for (int j = 0; j < keyList.size(); j++) {
                if (keyList.get(j).getKey() == keyData.get(i).getRoundKeyToGuess()) {
                    keyData.get(i).setPositionOfRoundKey(j);
                    break;
                }
            }
        }

        System.out.println("Statisctics data:");
        keyData.stream().forEach((keyData1) -> {
            keyData1.print();
        });
        System.out.println("Evaluation:");
        countPositions();
    }

    //there are 2^32 different plain texts, return next as array [?,?,?,?] 
    private int[] getNextPT() {
        int[] array = {0, 0, 0, 0};
        array[0] = counter & 0xff000000;
        array[1] = counter & 0xff0000;
        array[2] = counter & 0xff00;
        array[3] = counter & 0x0000ff;
        this.counter++;
        return array;
    }

    void generateRandomPT() {
        for (int i = 0; i < plainText.length; i++) {
            plainText[i] = rng.nextInt(max);
        }
    }

    // return CT as array to inputPT, 
    private int[] getCorrespondingCT(int[] array) {
        anubis = new Anubis(array, key, rounds);
        anubis.keySchedule();
        anubis.encrypt();
        return anubis.outputAsIntArray();
    }

    private boolean getBitAtPos(int value, int index) {
        int result = (value >>> index) & 1;
        return result == 1;
    }

    /* Three active S-boxes S+L
     * 1st round: 2e80 -> 006c, 2e80 -> 006c
     * 2nd round: 6c6c -> dddd
     */
    private boolean bitXor(int u1, int u2) {
        boolean result = false;
        boolean u1Xor = getBitAtPos(u1, 0) ^ getBitAtPos(u1, 2) ^ getBitAtPos(u1, 3) ^ getBitAtPos(u1, 4) ^ getBitAtPos(u1, 6) ^ getBitAtPos(u1, 7);
        boolean u2Xor = getBitAtPos(u2, 0) ^ getBitAtPos(u2, 2) ^ getBitAtPos(u2, 3) ^ getBitAtPos(u2, 4) ^ getBitAtPos(u2, 6) ^ getBitAtPos(u2, 7);

        //bit xor of chosen bits of plaintext, u1 and u2
        result = getBitAtPos(plainText[0], 1) ^ getBitAtPos(plainText[0], 2) ^ getBitAtPos(plainText[0], 3) ^ getBitAtPos(plainText[0], 5)
                ^ getBitAtPos(plainText[1], 1) ^ getBitAtPos(plainText[1], 2) ^ getBitAtPos(plainText[1], 3) ^ getBitAtPos(plainText[1], 5)
                ^ getBitAtPos(plainText[2], 7) ^ getBitAtPos(plainText[3], 7)
                ^ u1Xor ^ u2Xor;

        return result;
    }

    public void cryptanalysis() {
        keyList = new ArrayList<>();
        Key tempKey;
        int u1, u2, v1, v2;
        int k1, k2; //bytes of tempKey

        for (int i = 0; i < numOfKeys; i++) { //for all candidates keys
            tempKey = new Key(i);
            keyList.add(tempKey);
            k1 = i >>> 8; //upper 8 bits
            k2 = i & 0xff; //lower 8 bits

            for (int j = 0; j < numOfPairs; j++) { //for all pairs (OT,CT)
                plainText = pairList.get(j).pt;
                cipherText = pairList.get(j).ct;
                v1 = cipherText[1] ^ k1;
                v2 = cipherText[3] ^ k2;
                u1 = sbox[v1];
                u2 = sbox[v2];
                if (bitXor(u1, u2)) {
                    keyList.get(i).add();
                }
            }

            //count bias 
            keyList.get(i).countBias(numOfPairs);
            //System.out.println("Bias of key " + i + ": " + String.format("%.5f", keyList.get(i).getBias()));
        }
        Collections.sort(keyList);
    }

    public void printFirstNKeys(int n) {
        for (int i = 0; i < n; i++) {
            System.out.println("Bias of key " + keyList.get(i).getKey() + ": " + String.format("%.5f", keyList.get(i).getBias()));
        }
    }

    private void preparePairs() {
        pairList = new ArrayList<>();
        Pair temp;
        int[] array;

        for (int i = 0; i < numOfPairs; i++) {
            //array = getNextPT();
            generateRandomPT();
            temp = new Pair(plainText, getCorrespondingCT(plainText));
            //temp.print();
            pairList.add(temp);
        }

    }

}
