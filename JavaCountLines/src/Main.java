import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) throws IOException {
        assert(args.length == 2);
        long startTime = System.currentTimeMillis();
        // first param defines how many measurements should be taken (10 -> 10 times at a distance of 1 sec)
        MemoryUsageMonitor monitor = new MemoryUsageMonitor(Integer.parseInt(args[0]));
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();
        //args[1]: name of the .txt file, here "large_text_file.txt", args[2] Regular expression, here: ".*\.txt$"
        //the entire file is read in here, to change it to line based reading use the countLinesInAllFilesLineByLine method
        System.out.println("Lines: " + countLinesInAllFilesCompleteFile(args[1],args[2]));
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Duration: " + duration);
    }

    public static long countLines(String fileName) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(fileName));
        return allLines.size();
    }

    public static long countLinesInAllFilesCompleteFile(String folderPath, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths
//                    .peek(p -> System.out.println("Traversing: " + p.toString()))
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
//                    .peek(p -> System.out.println("Matching: " + p))
                    .mapToLong(path -> {
                        try {
                            return countLines(path.toString());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .sum();
        }
    }

    public static long countLinesInAllFilesLineByLine(String folderPath, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .mapToLong(path -> {
                        try {
                            return countLines(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .sum();
        }
    }

    private static long countLines(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            return reader.lines().count();
        }
    }
}