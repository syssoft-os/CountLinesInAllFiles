import java.io.*;
import java.util.Random;

public class RandomTextFileGenerator {

    private long bytesWritten = 0;
    private final long fileSizeInBytes;
    private final String fileName;

    public RandomTextFileGenerator(double gb, String fileName) {
        this.fileName = fileName;
        if (gb < 0) { throw new IllegalArgumentException("File size cannot be negative"); }
        this.fileSizeInBytes = (long) (gb * 1024 * 1024 * 1024);
    }

    public void generateRandomTextFile() throws IOException {

        // Die Implementierung durch einen Stream ermöglicht es dem Writer, Dateien zu schreiben, die größer sind als
        // der verfügbare Gesamtspeicher und darüber hinaus auch der Arbeitsspeicher.
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
        Random random = new Random();

        int lines = 0;
        while (bytesWritten < fileSizeInBytes) {
            char randomChar = getRandomChar();
            writer.write(randomChar);

            // 4% chance of adding a line break
            if (random.nextInt(100) < 4) {
                lines++;
                writer.write(System.lineSeparator());
            }

            bytesWritten++;
        }

        System.out.println(lines+" lines written.");
    }

    private char getRandomChar() {
        Random random = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int index = random.nextInt(characters.length());
        return characters.charAt(index);
    }
}
