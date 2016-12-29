/*
 * This class prepares data and save them into binary file.
 * Data are ready to use in various statistical tests.
 */
package utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Math.toIntExact;

/**
 *
 * @author Sarka Hatasova
 */
public class Statistics {

    private long numOfBytes;
    private byte[] byteStream;
    private String nameOfFile;
    private long counter;

    public Statistics(long numOfBytes, String nameOfFile) {
        if (numOfBytes < 1) {
            System.out.println("Creating statistical file: Wrong number of bits. Try it again.");
            return;
        }
        this.numOfBytes = numOfBytes;
        this.nameOfFile = nameOfFile;
        this.counter = 0;
        this.byteStream = new byte[toIntExact(numOfBytes)];
    }


    private void writeByteStreamIntoFile() throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(nameOfFile)) {
            fos.write(byteStream, 0, byteStream.length);
            fos.flush();
            System.out.println("Into file "+ nameOfFile+" was written "+byteStream.length + " bytes of (binary) data.");     
         //   System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(byteStream));
        }
        
    }

    public void addByteStream(byte[] stream) throws IOException {
        if (counter + stream.length > (numOfBytes)) {
            writeByteStreamIntoFile();
            return;
        }
        for (int i = 0; i < stream.length; i++) {
            this.byteStream[toIntExact(counter) + i] = stream[i];
          //  System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(byteStream));
        }

        counter += stream.length;
        if (counter == numOfBytes) {
            writeByteStreamIntoFile();
        }
    }


}
