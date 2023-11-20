import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;

public class CreateTextFile {

    private static final long BYTES_IN_GIGABYTE = 1024L * 1024L * 1024L;

    public static void main(String[] args) {
        /* args:
         * 0: file path
         * 1: file size (gb)
         * 2: min length for generated lines
         * 3: max length for generated lines
         * 4: (opt) number of files to create
         */
        int fileCount = args.length > 4 ? Integer.parseInt(args[4]) : 1;
        String filePath = args[0];
        String fileExt = "";
        if (fileCount > 1) {
            int extBegin = filePath.lastIndexOf('.');
            if (extBegin >= 0) {
                fileExt = filePath.substring(extBegin);
                filePath = filePath.substring(0, extBegin);
            }
        }

        final String pathFinal = filePath;
        final String fileExtFinal = fileExt;

        double sizeInGB = Double.parseDouble(args[1]);
        int minLineLength = Math.max(Integer.parseInt(args[2]), 0);
        int lengthRange = Math.max(Integer.parseInt(args[3]) - minLineLength, 1);

        long totalBytes = (long)(sizeInGB * BYTES_IN_GIGABYTE);

        if (totalBytes <= 0) {
            System.out.println("file size has to be larger than 0 bytes");
            return;
        }

        if (fileCount == 1)
            generateFile(pathFinal, totalBytes, minLineLength, lengthRange);
        else
            IntStream.range(0, fileCount).parallel().forEach(i -> generateFile(pathFinal + i + fileExtFinal, totalBytes, minLineLength, lengthRange));
    }

    static void generateFile(String path, long totalBytes, int minLineLength, int lengthRange)
    {
        Random rnd = new Random();
        try (FileWriter writer = new FileWriter(path)) {
            char[] line = new char[minLineLength + lengthRange];
            for (long bytesRemaining = totalBytes; bytesRemaining > 0;) {
                int lineLength = rnd.nextInt(lengthRange) + minLineLength;
                int charsToWrite = (int)Math.min(bytesRemaining, lineLength + 1);
                generateRandomLine(line, charsToWrite - 1, rnd);
                writer.write(line, 0, charsToWrite);
                bytesRemaining -= charsToWrite;
            }
            System.out.format("created file '%s'\n", path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Generate a random line with ASCII characters (a-z, A-Z, 0-9) */
    private static void generateRandomLine(char[] line, int charCount, Random rnd) {
        for (int i = 0; i < charCount; ++i) {
            int number = rnd.nextInt(26 + 26 + 10);
            if (number - 26 - 26 >= 0)
                line[i] = (char)(number - 26 - 26 + '0');
            else if (number - 26 >= 0)
                line[i] = (char)(number - 26 + 'A');
            else
                line[i] = (char)(number + 'a');
        }
        line[charCount] = '\n';
    }
}
