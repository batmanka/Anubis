/*
 * Loader of results of Linear Cryptanalysis of Large S-box
 * =============================================================
 * Read from file /data/large_sbox_biases.txt
 * 
 */
package linearcryptanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author Sarka Hatasova
 */
public class Loader {

    ArrayList<Item> itemList;
    String pathString;
    int[] path;
    int[] sbox1;
    int[] bias1;
    int[] sbox2;
    int[] bias2;

    public Loader() {
        path = new int[10];
    }

    private void createItem(String line) {
        Item tempItem;
        int bias, inputSum, outputSum;
        bias = Integer.parseInt(line.substring(0, line.indexOf(' ')));
        line = line.substring(line.indexOf('=') + 4);
        tempItem = new Item(bias);

        while (true) {
            inputSum = Integer.parseInt(line.substring(0, line.indexOf(',')), 16);
            line = line.substring(line.indexOf(',') + 1);
            outputSum = Integer.parseInt(line.substring(0, line.indexOf('}')), 16);
            line = line.substring(line.indexOf(',') + 1);
            tempItem.addAppearance(inputSum, outputSum);

            if (line.contains("{")) {
                line = line.substring(line.indexOf('{') + 1);
            } else {
                break;
            }
        }
        itemList.add(tempItem);
    }

    //read file created by LargeSBoxAnalysis and store data as Items
    public ArrayList<Item> readFromFile() throws IOException {
        itemList = new ArrayList<>();
        String nameOfFile = "large_sbox_biases.txt";
        String directory = "data";
        BufferedReader br;
        FileInputStream fis;
        File dir;
        File file;
        dir = new File(".");
        file = new File(dir.getCanonicalPath() + File.separator + directory + File.separator + nameOfFile);

        fis = new FileInputStream(file);
        br = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        System.out.println("Reading from file...");
        while ((line = br.readLine()) != null) {
            createItem(line); //one bias with its appearances is loaded                    
        }
        br.close();
        System.out.println("Data from file was loaded.");  
        return itemList;
    }
}
