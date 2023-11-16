import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.*;
import java.util.regex.*;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException {
        assert(args.length == 2);

        //RandomTextFileGenerator gen = new RandomTextFileGenerator(1,"random.txt");
        //gen.generateRandomTextFile();

        // lese CSV mit beliebigen CSV-Plotter ein.
        // z.B: https://chart-studio.plotly.com/create/

        MemoryUsageChecker checker = new MemoryUsageChecker("LOG");

        //MemoryUsageChecker checker = new MemoryUsageChecker("LOG");

        /*
        checker.observe(5);
        System.out.println(countLines("random.txt"));
        */

        /*
        checker.observe(5);
        System.out.println(countLinesBuiltIn("random.txt"));
        /*

        // System.out.println(countLinesStream("random.txt"));  // schreckliche Performance

        /*
        checker.observe(5);
        System.out.println(countLinesNumberReader("random.txt"));
        */

        checker.observe(5);
        System.out.println(countLinesParallel("random.txt",1));

        /*
        Beschreibung:
        Im Heap-Speicher der JVM werden unter anderem die Java-Objekte, Arrays, Strings und der Garbage-Collector
        verwaltet. Unter den gespeicherten Daten sind auch nicht notwendige Daten enthalten, welche bei Bedarf
        gelöscht werden können.
        Werden die Grenzen des maximalen Heap-Memory erreicht, so wird die JVM zunächst versuchen, Speicherplatz
        durch ein Entfernen von unnötigen Daten wiederherzustellen. Ist dies nicht möglich, so wird der Gesamtspeicher
        erweitert - also mehr Speicher für die JVM reserviert. Dies kann aber nur erfolgen, bis die Kapazitäten des
        Arbeitsspeichers erreicht sind.
        */

        // Regular expression ".*\.c$" matches any c source files
        // System.out.println(countLinesInAllFiles(args[0],args[1]));
    }

    /*
    Bewertung countLines: Diese Implementierung lädt jede Zeile der Datei als einzelne Strings und die Liste als ein
    komplexes Objekt mit vielen Pointern in den Heap-Speicher. Dadurch wird letztendlich sogar mehr Speicher benötigt,
    als die eingelesene Datei groß ist. Ein SCHLECHTER Ansatz.
     */
    public static long countLines(String fileName) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(fileName));
        return allLines.size();
    }

    /*
    Bewertung countLinesBuiltIn: Diese Implementierung lädt die ganze Datei als ein Objekt. Dadurch ver-
    braucht diese Variante nur marginal mehr Heap-Speicher, als die Datei eigentlich groß ist. Ein BESSERER Ansatz.
     */
    public static long countLinesBuiltIn(String fileName) throws IOException {
        return Files.lines(Path.of(fileName)).count();
    }

    /*
    Bewertung countLinesStream: Diese Implementierung von liest die Datei Zeilenweise und verbraucht dadurch kaum
    Heap-Speicherplatz. Dafür braucht sie, insbesondere für große Dateien, viel zu lange, denn jedes Byte wird einzeln
    untersucht. Ein SCHRECKLICHER Ansatz.
    Hinweis: Wenn wir die Funktion durch einen InputStreamReader ergänzen, sorgt dies dafür, dass wieder die ganze Datei
    wieder in den Heap-Speicher geladen wird - d.h. selber Effekt wie bei countLinesBuiltIn.
     */
    public static long countLinesStream(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        int byteRead;
        long lineCount = 0;

        while ((byteRead = fis.read()) != -1) {
            if (byteRead == '\n') { lineCount++; }
        }

        return lineCount;
    }

    /*
    Bewertung countNumberReader: Diese Implementierung verwendet den LineNumberReader. Dieser lädt nicht die ganze
    Datei ein, arbeitet aber vergleichbar schnell. Er benötigt nur marginal mehr Heap-Speicher-Platz als die
    Stream-Variante. Ein GUTER Ansatz.
     */
    public static int countLinesNumberReader(String filePath) throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(filePath));

        try {
            while (reader.skip(Long.MAX_VALUE) > 0) { }
            return reader.getLineNumber() + 1;
        } finally {
            reader.close();
        }
    }

    /*
    Bewertung countNumberParallel: Diese Implementierung optimiert den Ansatz von countNumberStream und optimiert ihn
    durch Threading. Er zerstückelt die Datei und lässt jedes Stück durch einen eigenen Thread untersuchen. Abgesehen
    von der zu langsamen Stream-Variante scheint sie den geringsten Heap-Speicherplatz zu verbrauchen. Zusammenfassend
    ein komplizierter, aber SEHR GUTER Ansatz.
     */
    public static Object countLinesParallel(String fileName, int mb) throws IOException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            ParallelLineCounterTask task = new ParallelLineCounterTask(fis, 0, fis.available(),mb);
            return forkJoinPool.invoke(task);
        }
    }

    static class ParallelLineCounterTask extends RecursiveTask<Long> {
        private final int chunk_size;
        private final FileInputStream fis;
        private final long start;
        private final long end;

        public ParallelLineCounterTask(FileInputStream fis, long start, long end, int mb) {
            this.fis = fis;
            this.start = start;
            this.end = end;
            this.chunk_size = mb * 1024 * 1024;
        }

        @Override
        protected Long compute() {
            long count = 0;
            byte[] buffer = new byte[chunk_size];

            try {
                fis.skip(start);
                long bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1 && bytesRead + start <= end) {
                    for (int i = 0; i < bytesRead; i++) {
                        if (buffer[i] == '\n') {
                            count++;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return count;
        }
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