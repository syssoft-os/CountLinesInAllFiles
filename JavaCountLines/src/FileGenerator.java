import java.io.*;
import java.lang.Math;

public class FileGenerator{

    private String charString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
    private long currentSize = 0;

    public FileGenerator(double filesize, File f) throws IOException{
        if (filesize <= 0) throw new IllegalArgumentException("File size must be greater than 0");
        long size = (long) (filesize * Math.pow( 1.074*10 , 9));
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        try {
            while (currentSize < size){
            int lineLength = (int)(Math.random() * 1000);
            String line = "";
            for (int i = 0; i < lineLength; i++){
                line += charString.charAt((int)(Math.random() * charString.length()));
            }
            writer.write(line);
            writer.newLine();
            currentSize += 2 * lineLength;
        }
        } catch (Exception e){
            System.out.println(e);
        }
        if (writer != null) writer.close();
    }
}