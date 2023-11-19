import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) throws IOException {
        assert(args.length == 2);
        // Regular expression ".*\.c$" matches any c source files
        //System.out.println(countLinesInAllFiles(args[0],args[1]));

        final String fileName = "New1GBFile.txt";
        File f = new File(fileName);
        FileGenerator fg;
        if (!f.exists() && !f.isDirectory()) fg = new FileGenerator(1d, f);

        MemoryLogManager mm = new MemoryLogManager(5,true);
        mm.start();

        /* 
        long startTime = System.currentTimeMillis();

        //Aufgabe 4
        //Laufzeit: 2708ms, sehr hohe Speicherauslastung. 
        long lineCount = countLines(f.getPath());

        long endTime = System.currentTimeMillis();

        System.out.println(lineCount + "lines in: " + (endTime-startTime) + "ms");
        */
        

        //Aufgabe 5

        long startTime = System.currentTimeMillis();
        long lineCount = 0;

        //5.1 Implementation durch BufferedReader
        //Laufzeit: 1545ms, deutliche Reduktion der Speicherauslastung im Vergleich zu 4. 
        /*
        BufferedReader bf = new BufferedReader(new FileReader(f));
        while (bf.readLine() != null) lineCount++;
         */
        
        //5.2 Implementation mit Files.lines und Stream
        //Laufzeit: 1456ms, minimale reduktion der Speicherauslastung zu Beginn. Anschließend nahezu identisch (Siehe Logs/Graphen) verglichen mit 5.1. Dennoch großer Unteschied zu 4
        try {
            Stream<String> stream = Files.lines(Path.of(f.getPath()));
            lineCount = (int) stream.count();
            stream.close();
        } catch (IOException e) {System.out.println(e);} 
        

        long endTime = System.currentTimeMillis();
        System.out.println(lineCount + "lines in: " + (endTime-startTime) + "ms");
    }

    public static long countLines(String fileName) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(fileName));
        return allLines.size();
    }

    public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {
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
}