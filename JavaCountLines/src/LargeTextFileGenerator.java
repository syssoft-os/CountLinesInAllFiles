import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class LargeTextFileGenerator {

    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LINE_LENGTH = 80; // Zeichen pro Zeile
    private static final int CHARS_PER_LINE = 1000; // Gesamtzeichen pro Zeile

    private double fileSizeInBytes;

    public LargeTextFileGenerator(double fileSizeInGigabytes) {
        if (fileSizeInGigabytes <= 0) {
            throw new IllegalArgumentException("Dateigröße muss größer als 0 sein.");
        }
        this.fileSizeInBytes = fileSizeInGigabytes * 1024L * 1024L * 1024L;
    }

    public void generateFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            long bytesWritten = 0;

            while (bytesWritten < fileSizeInBytes) {
                String line = generateRandomLine();
                writer.write(line);
                bytesWritten += line.length();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateRandomLine() {
        StringBuilder lineBuilder = new StringBuilder(CHARS_PER_LINE);

        Random random = new Random();
        for (int i = 0; i < CHARS_PER_LINE; i++) {
            char randomChar = CHARSET.charAt(random.nextInt(CHARSET.length()));
            lineBuilder.append(randomChar);

            if ((i + 1) % LINE_LENGTH == 0) {
                lineBuilder.append(System.lineSeparator()); // Neue Zeile alle LINE_LENGTH Zeichen
            }
        }

        return lineBuilder.toString();
    }

    public static void main(String[] args) {
        try {
            double fileSizeInGigabytes = Double.parseDouble(args[0]);
            LargeTextFileGenerator generator = new LargeTextFileGenerator(fileSizeInGigabytes);
            generator.generateFile("large_text_file.txt");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Bitte geben Sie eine positive Ganzzahl als Dateigröße in Gigabyte an.");
        }
    }
}
