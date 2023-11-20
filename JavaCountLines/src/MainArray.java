import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.stream.*;
import java.util.regex.*;

public class MainArray {

    public static void main(String[] args) throws IOException {
        /* args:
         * 0: folder path
         * 1: file name regex (".*\.c$" matches any c source files)
         * 2: (opt) read buffer size (bytes)
         */
        int charBufferSize = 1024 * 512 + 1;
        if (args.length > 2) {
            try {
                charBufferSize = Integer.parseInt(args[2]) + 1;
            } catch (Exception ignored) {
            }
        }
        System.out.println(countLinesInAllFiles(args[0], args[1], charBufferSize));
    }

    public static long countLines(String fileName, char[] chars) throws IOException {
        long lineCount = 0;
        try (FileReader reader = new FileReader(fileName))
        {
            int lastCharCount = 0;
            final int strLen = chars.length - 1;
            boolean ignoreLF = false;
            for (int charCount; (charCount = reader.read(chars, 0, strLen)) > 0;) {
                char c;
                for (int i = 0; i < charCount; ++i) {
                    c = chars[i];
                    lineCount += (c == '\r' | (!ignoreLF & c == '\n')) ? 1 : 0;
                    ignoreLF = c == '\r';
                }
                lastCharCount = charCount;
            }
            if (lastCharCount > 0 && (chars[lastCharCount - 1] != '\r' & chars[lastCharCount - 1] != '\n'))
                ++lineCount;
        }
        return lineCount;
    }

    public static long countLinesInAllFiles(String folderPath, String regex, int charBufferSize) throws IOException {
        final char[] chars = new char[charBufferSize];
        chars[chars.length - 1] = 0;
        final Pattern pattern = Pattern.compile(regex);
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            System.gc(); // halfes memory usage on my machine for some reason
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .mapToLong(path -> {
                        try {
                            return countLines(path.toString(), chars);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .sum();
        }
    }
}
