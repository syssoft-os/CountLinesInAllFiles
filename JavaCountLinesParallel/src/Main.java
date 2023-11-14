import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.stream.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) throws IOException {
        assert (args.length == 2);
        // Regular expression ".*\.c$" matches any c source files
        System.out.println(countLinesInAllFiles(args[0], args[1]));
    }

    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);
        long totalLines = 0;
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths.parallel()
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
}