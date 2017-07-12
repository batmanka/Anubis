/*
 * Implementation of BABY ANUBIS cipher 
 * =====================================
 * + encrypt or decrypt one data block (4-bytes/32-bits) with standard number of rounds (12) 
 * + encrypt or decrypt one data block (4-bytes/32-bits) with variable number of rounds (1-11) 
 * + analyze its S-BOXes (gamma layer) 8/8 bits
 * + analyze its large S-BOXes (gamma layer + theta layer) 16/16 bits
 * + creates variable output data for statistical purposes
 * + performs linear cryptanalysis of the 2-round cipher (=1 full round cipher)
 * + performs linear cryptanalysis of the 3-round cipher (=2 full round cipher)
 * + collects statistics data from 2 of 3 rounds attacks
 *
 * How to use this program
 * =========================
 * There are pre-built functionality that you can call uncommenting appropriate line in main()
 *
 * How to understand this project
 * ===============================
 * package mybabyanubis: 
 *   -core of everything, implementation of Baby Anubis cipher, cipher's support functions
 * package linearcryptanalysis:
 *   -implementation of (gamma layer + theta layer) S-box, analysis of original 8-bit S-box, 
 *    analysis of (gamma layer + theta layer) 16-bit S-box, two and three rounds attacks
 * package utility: 
 *   -other calcutation but not necessary for linear cryptanalysis
 *
 */
package mybabyanubis;

import java.io.IOException;
import java.util.Random;
import linearcryptanalysis.Explorer;
import linearcryptanalysis.TwoRoundsAnalysis;
import linearcryptanalysis.SBoxAnalysis8bits;
import utility.Statistics;
import linearcryptanalysis.ThreeRoundsAnalysisVersionA;
import linearcryptanalysis.ThreeRoundsAnalysisVersionB;

/**
 *
 * @author Sarka Hatasova
 */
public class MyBabyAnubis {

    static int[] plainText
            = {0, 0, 0, 0};
    static int[] cipherText
            = {246, 70, 102, 97}; //for PT {0, 0, 0, 0} and key {5, 2, 3, 100}

    static int[] key
            = {5, 2, 3, 100};

    static int counter = 0;
    static final int max = 255;
    static final int min = 0;

    static void generateRandomPT() {
        Random rn = new Random();
        for (int i = 0; i < plainText.length; i++) {
            plainText[i] = rn.nextInt((max - min) + 1) + min;
        }
    }

    static void generateRandomKey() {
        Random rn = new Random();
        for (int i = 0; i < key.length; i++) {
            key[i] = rn.nextInt((max - min) + 1) + min;
        }
    }

    /* The best biases from 8-bit S-box analysis */
    static int[] arraySum = {128, -34, -32, -30, -28/*, -26, 26*/};

    /* Two functions below are for statistics purposes only
     * They make output for testing changes in key extraction of Baby Anubis
     * You can read about this in my master thesis in section 4.3.3.
     * If you wanna repeat these tests again, change in Anubis.java values of V-matrix
     */
    static void createStatisticsDataOfCipherOutput() throws IOException {
        int numOfBytes = 100000; //number of bytes in output file
        Statistics stat = new Statistics(numOfBytes, "test3_100000_bytes");
        counter = 0;
        while (true) {
            // System.out.println("PT: "+plainText[0]+", "+plainText[1]+", "+ plainText[2]+", "+plainText[3]);
            Anubis anubis = new Anubis(plainText, key);
            anubis.keySchedule();
            anubis.encrypt();
            stat.addByteStream(anubis.outputAsByteArray());
            counter += 4;
            generateRandomPT();
            generateRandomKey();
            if (counter >= numOfBytes) {
                break;
            }
        }
    }

    static void createStatisticsDataOfKeys() throws IOException {
        int numOfBytes = 48000;
        int roundsPerOneKey = 100;
        Statistics stat = new Statistics(numOfBytes, "key_stream");
        counter = 0;
        while (true) {
            // System.out.println("PT: "+plainText[0]+", "+plainText[1]+", "+ plainText[2]+", "+plainText[3]);
            generateRandomKey();
            Anubis anubis = new Anubis(plainText, key);
            stat.addByteStream(anubis.get100KesInBytes(roundsPerOneKey));
            counter += roundsPerOneKey * 4; //100x 8bit informations = 100 bytes,  100 x 4 (4 elements in matrices) = 400

            if (counter >= numOfBytes) {
                break;
            }
        }
    }

    /* analyzeSBox() analyzes original 8-bits S-BOXes of Anubis for linear cryptanalysis 
     * and prints results, one instance of class Anubis must exist before calling this function
     */
    static void analyzeSBox() {
        Anubis anubis = new Anubis(plainText, key);
        SBoxAnalysis8bits analysis = new SBoxAnalysis8bits();
        analysis.analyzeSBox();
        analysis.printBiases();
        analysis.printTable(); //prints linear approximation table

        /* By uncommenting line below you can check one specific value of bias 
         * and find out its location(s) {INPUT SUM, OUTPUT SUM}
         */
        //analysis.printIOSumsOfBias(-32);
    }

    /*
     * explorePaths() counts a certain part of linear approximation table of large 16-bits S-box
     * ! this takes about 20 hours !
     */
    static void explorePaths() {
        Explorer explorer = new Explorer();
        explorer.exhaustiveSearch();
    }

    /*
     * loadAndFindPaths() reads output from explorePaths() saved in file
     * and looks foor a path through 2 or 3 S-boxes
     */
    static void loadAndFindPaths() throws IOException {
        Explorer explorer = new Explorer();
        explorer.load();
        explorer.findPathFor2SBoxes();
        //explorer.findPathFor3SBoxes();
    }

    /* 
     * Two rounds Baby Anubis attack
     */
    static void twoRoundsCryptanalysis() {
        /* Constructor's first parameter: how many random cipher keys are tested,
         * second parameter: how many samples of (OT,CT) are prepared
         * !carefully, reducing the number of samples deteriorating results
         * One key + 500 samples takes few seconds
         * One key + 65536 samples takes about 3 minutes
         */
        TwoRoundsAnalysis a = new TwoRoundsAnalysis(20, 500);
        a.collectStaticticsData();
        /* Or use constructor and lines belowfor one cipher key you wish
         * and comment constructor and lines above
         */
        /*
         OneRoundAnalysis a = new OneRoundAnalysis(key, 65536);
         a.cryptanalysis();
         a.printFirstNKeys(50);
         */
    }

    /* 
     * Three rounds Baby Anubis attack
     * with two active S-boxes
     */
    static void threeRoundsCryptanalysisTwoActiveSBoxes() {
        ThreeRoundsAnalysisVersionA a = new ThreeRoundsAnalysisVersionA(10, 65536);
        a.collectStaticticsData();
    }

    /* 
     * Three rounds Baby Anubis attack
     * with three active S-boxes
     */
    static void threeRoundsCryptanalysisThreeActiveSBoxes() {
        ThreeRoundsAnalysisVersionB b = new ThreeRoundsAnalysisVersionB(10, 65536);
        b.collectStaticticsData();
    }

    static void showHowAnubisWorks() {
        System.out.println("This is a small sample how Anubis works - encrypts the plaintext and decrypts it back (with the same key of course).");
        Anubis anubis = new Anubis(plainText, key);
        anubis.keySchedule();
        anubis.encrypt();
        anubis.printInputText();
        anubis.printOutputText();
        anubis.printOutputTextInt();
        System.out.println("");

        Anubis anubis2 = new Anubis(cipherText, key);
        anubis2.keySchedule();
        anubis2.decrypt();
        anubis2.printInputText();
        anubis2.printOutputText();
    }

    public static void main(String[] args) throws IOException {
        showHowAnubisWorks();
        
        //or you can call another functionality by uncommenting one of lines below:
        
        //analyzeSBox();
        // explorePaths();
        // loadAndFindPaths();
        //twoRoundsCryptanalysis();
        // threeRoundsCryptanalysisTwoActiveSBoxes();
        // threeRoundsCryptanalysisThreeActiveSBoxes();
        //uncomment 2 line below if you want to get statistics data (ready for statistical test suite NIST)
        /*       
         createStatisticsDataOfCipherOutput();
         createStatisticsDataOfKeys();
         */
    }

}
