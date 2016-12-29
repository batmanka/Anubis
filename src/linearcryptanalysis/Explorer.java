/*
 * Explorer works with LargeSboxAnalysis and looks for the best way through Large Sbox
 */
package linearcryptanalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Sarka Hatasova
 */
public class Explorer {

    //input sums composite from best inputs into 8-bit SBox
    private final int[] bestInputSums = {13, 20, 26, 46, 90, 96, 108, 115, 118, 128, 158,
        169, 188, 203, 210, 221, 225, 226, 3328, 3341, 3348, 3354, 3374, 3418, 3424, 3436, 3443,
        3446, 3456, 3486, 3497, 3516, 3531, 3538, 3549, 3553, 3554, 5120, 5133, 5140, 5146,
        5166, 5210, 5216, 5228, 5235, 5238, 5248, 5278, 5289, 5308, 5323, 5330, 5341, 5345,
        5346, 6656, 6669, 6676, 6682, 6702, 6746, 6752, 6764, 6771, 6774, 6784, 6814, 6825,
        6844, 6859, 6866, 6877, 6881, 6882, 11776, 11789, 11796, 11802, 11822, 11866, 11872,
        11884, 11891, 11894, 11904, 11934, 11945, 11964, 11979, 11986, 11997, 12001, 12002,
        23040, 23053, 23060, 23066, 23086, 23130, 23136, 23148, 23155, 23158, 23168, 23198,
        23209, 23228, 23243, 23250, 23261, 23265, 23266, 24576, 24589, 24596, 24602, 24622,
        24666, 24672, 24684, 24691, 24694, 24704, 24734, 24745, 24764, 24779, 24786, 24797,
        24801, 24802, 27648, 27661, 27668, 27674, 27694, 27738, 27744, 27756, 27763, 27766,
        27776, 27806, 27817, 27836, 27851, 27858, 27869, 27873, 27874, 29440, 29453, 29460,
        29466, 29486, 29530, 29536, 29548, 29555, 29558, 29568, 29598, 29609, 29628, 29643,
        29650, 29661, 29665, 29666, 30208, 30221, 30228, 30234, 30254, 30298, 30304, 30316,
        30323, 30326, 30336, 30366, 30377, 30396, 30411, 30418, 30429, 30433, 30434, 32768,
        32781, 32788, 32794, 32814, 32858, 32864, 32876, 32883, 32886, 32896, 32926, 32937,
        32956, 32971, 32978, 32989, 32993, 32994, 40448, 40461, 40468, 40474, 40494, 40538,
        40544, 40556, 40563, 40566, 40576, 40606, 40617, 40636, 40651, 40658, 40669, 40673,
        40674, 43264, 43277, 43284, 43290, 43310, 43354, 43360, 43372, 43379, 43382, 43392,
        43422, 43433, 43452, 43467, 43474, 43485, 43489, 43490, 48128, 48141, 48148, 48154,
        48174, 48218, 48224, 48236, 48243, 48246, 48256, 48286, 48297, 48316, 48331, 48338,
        48349, 48353, 48354, 51968, 51981, 51988, 51994, 52014, 52058, 52064, 52076, 52083,
        52086, 52096, 52126, 52137, 52156, 52171, 52178, 52189, 52193, 52194, 53760, 53773,
        53780, 53786, 53806, 53850, 53856, 53868, 53875, 53878, 53888, 53918, 53929, 53948,
        53963, 53970, 53981, 53985, 53986, 56576, 56589, 56596, 56602, 56622, 56666, 56672,
        56684, 56691, 56694, 56704, 56734, 56745, 56764, 56779, 56786, 56797, 56801, 56802,
        57600, 57613, 57620, 57626, 57646, 57690, 57696, 57708, 57715, 57718, 57728, 57758,
        57769, 57788, 57803, 57810, 57821, 57825, 57826, 57856, 57869, 57876, 57882, 57902,
        57946, 57952, 57964, 57971, 57974, 57984, 58014, 58025, 58044, 58059, 58066, 58077,
        58081, 58082};
    private final int size = 65536;
    private final double doubleSize = 65536.0;
    private ArrayList<Item> listOfAppearances;
    private final int numBestBiases = 12; //12 is ok for 2 rounds search, 50 for 3 rounds
    private int[] bestItemsInputSum;
    private int[] bestItemsOutputSum;
    private ArrayList<Path> pathList;

    public Explorer() {

    }

    public void exhaustiveSearch() {
        System.out.println("Exhaustive search starts.");
        listOfAppearances = new ArrayList<>();
        LargeSBox largeSBox = new LargeSBox();
        largeSBox.fillLargeSBox();
        LargeSBoxAnalysis analysis = new LargeSBoxAnalysis(largeSBox);
        int value, index;
        Item tempItem, tempItem2;

        for (int i = 0; i < bestInputSums.length; i++) {
            //System.out.println("i=" + i);
            for (int j = 0; j < size; j++) {
                value = analysis.analyzeOneValueAtPosition(bestInputSums[i], j);
                //store value
                if (Math.abs(value) < 500) {
                    continue;
                }

                tempItem = new Item(value);
                if (listOfAppearances.contains(tempItem)) {//item already exists
                    index = listOfAppearances.indexOf(tempItem);
                    listOfAppearances.get(index).addAppearance(bestInputSums[i], j);
                } else {
                    tempItem2 = new Item(value, bestInputSums[i], j);
                    listOfAppearances.add(tempItem2);
                }
            }
        }
        Collections.sort(listOfAppearances);
        System.out.println("Exhaustive search ends.");
        printBiases();
        printAll();
    }

    public void load() throws IOException {
        Loader loader = new Loader();
        listOfAppearances = loader.readFromFile();
        //printBiases();
    }

    //bias of whole approximation, piling up lemma is used here
    private double countBias(double bias1, double bias2) {
        double result = 2 * (bias1 / size) * (bias2 / size);
        return result;
    }

    private double countBias(double bias1, double bias2, double bias3) {
        double result = 4 * (bias1 / size) * (bias2 / size) * (bias3 / size);
        return result;
    }

    public void findPathFor2SBoxes() {
        pathList = new ArrayList<>();
        Path tempPath;
        int numOfItems = 0;
        int cnt = 0;
        double bias;
        int[] biasArray;

        for (int i = 0; i < numBestBiases; i++) {
            numOfItems += listOfAppearances.get(i).getSizeOfInputSums();
        }

        bestItemsInputSum = new int[numOfItems];
        bestItemsOutputSum = new int[numOfItems];
        biasArray = new int[numOfItems];

        //fill arrays
        for (int i = 0; i < numBestBiases; i++) {
            for (int j = 0; j < listOfAppearances.get(i).getSizeOfInputSums(); j++) {
                bestItemsInputSum[cnt] = listOfAppearances.get(i).getInputAtIndex(j);
                bestItemsOutputSum[cnt] = listOfAppearances.get(i).getOutputAtIndex(j);
                biasArray[cnt] = listOfAppearances.get(i).getBias();
                cnt++;
            }
        }

        System.out.println("Number of items with best biases is " + numOfItems);

        for (int i = 0; i < numOfItems; i++) {
            //first or second byte is equal zero
            if (bestItemsInputSum[i] < 0x100 || ((bestItemsInputSum[i] & 0x00ff) == 0)) {

                for (int j = 0; j < listOfAppearances.size(); j++) {
                    for (int k = 0; k < listOfAppearances.get(j).getSizeOfInputSums(); k++) {
                        if (listOfAppearances.get(j).getOutputAtIndex(k) == bestItemsInputSum[i]) {
                            bias = countBias(biasArray[i], listOfAppearances.get(j).getBias());
                            tempPath = new Path(listOfAppearances.get(j).getInputAtIndex(k), listOfAppearances.get(j).getOutputAtIndex(k),
                                    bestItemsInputSum[i], bestItemsOutputSum[i], bias
                            );
                            pathList.add(tempPath);
                        }
                    }
                }
            }
        }
        Collections.sort(pathList);
        System.out.println(pathList.size() + " approximation found.");
        for (int i = 0; i < pathList.size(); i++) {
            pathList.get(i).print();
        }

    }

    public void findPathFor3SBoxes() {
        pathList = new ArrayList<>();
        Path tempPath;
        int numOfItems = 0;
        int cnt = 0;
        double bias;
        int[] biasArray;
        int byteA, byteB;

        for (int i = 0; i < numBestBiases; i++) {
            numOfItems += listOfAppearances.get(i).getSizeOfInputSums();
        }

        bestItemsInputSum = new int[numOfItems];
        bestItemsOutputSum = new int[numOfItems];
        biasArray = new int[numOfItems];

        //fill arrays
        for (int i = 0; i < numBestBiases; i++) {
            for (int j = 0; j < listOfAppearances.get(i).getSizeOfInputSums(); j++) {
                bestItemsInputSum[cnt] = listOfAppearances.get(i).getInputAtIndex(j);
                bestItemsOutputSum[cnt] = listOfAppearances.get(i).getOutputAtIndex(j);
                biasArray[cnt] = listOfAppearances.get(i).getBias();
                cnt++;
            }
        }

        System.out.println("Number of items with best biases is " + numOfItems);

        for (int i = 0; i < numOfItems; i++) {

            byteA = bestItemsInputSum[i] & 0xff00;
            byteB = bestItemsInputSum[i] & 0x00ff;

            for (int j = 0; j < listOfAppearances.size(); j++) {
                for (int k = 0; k < listOfAppearances.get(j).getSizeOfInputSums(); k++) {
                    if (((listOfAppearances.get(j).getOutputAtIndex(k) & 0xff00) == byteA && (listOfAppearances.get(j).getOutputAtIndex(k) & 0x00ff) == 0)) {
                        //we have a half of path
                        for (int l = 0; l < listOfAppearances.size(); l++) {
                            for (int m = 0; m < listOfAppearances.get(l).getSizeOfInputSums(); m++) {
                                if ((listOfAppearances.get(l).getOutputAtIndex(m) & 0x00ff) == byteB && (listOfAppearances.get(l).getOutputAtIndex(m) & 0xff00) == 0) {
                                    //we have all
                                    bias = countBias(biasArray[i], listOfAppearances.get(j).getBias(), listOfAppearances.get(l).getBias());
                                    tempPath = new Path(listOfAppearances.get(j).getInputAtIndex(k), listOfAppearances.get(j).getOutputAtIndex(k),
                                            listOfAppearances.get(l).getInputAtIndex(m), listOfAppearances.get(l).getOutputAtIndex(m),
                                            bestItemsInputSum[i], bestItemsOutputSum[i], bias);
                                    pathList.add(tempPath);
                                }//end if
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(pathList);//sort paths acording bias of whole approximation
        System.out.println(pathList.size() + " approximation found.");
        for (int i = 0; i < pathList.size(); i++) {
            pathList.get(i).print();
        }
    }

    
    
    //this function counts appearances of biases - just for better print of results
    public void printBiases() {
        listOfAppearances.stream().forEach((listOfAppearance) -> {
            listOfAppearance.printItem();
        });
    }

    public void printAll() {
        listOfAppearances.stream().forEach((listOfAppearance) -> {
            listOfAppearance.getBias();
            listOfAppearance.printAppearanceAsArray();

        });

    }

    public void printMidResult() {
        Collections.sort(listOfAppearances);
        printBiases();
        printAll();
    }

    public int getListSize() {
        return listOfAppearances.size();
    }

}
