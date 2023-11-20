import java.io.*;
import java.nio.file.*;
import java.util.stream.*;
import java.util.regex.*;

public class MainLineNumberReader {
    public static void main(String[] args) throws IOException {
        /* args:
         * 0: folder path
         * 1: file name regex (".*\.c$" matches any c source files)
         * 2: (opt) read buffer size (bytes)
         */
        System.out.println(countLinesInAllFiles(args[0],args[1]));
    }

    public static long countLines(String fileName) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader(fileName))) {
            while (reader.skip(Long.MAX_VALUE) > 0);
            return reader.getLineNumber() + 1;
        }
    }

    public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {
        final Pattern pattern = Pattern.compile(regex);
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            System.gc(); // quarters memory usage on my machine for some reason
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
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
