import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class MemoryUsageChecker {
    private String mode = "LOG";
    private String fileName = "";

    public MemoryUsageChecker(String mode) { this.mode = mode; }

    public void observe(int time) {
        if ("CSV".equals(this.mode)) {
            int i=1;
            while(Files.exists(Path.of("memory_" + String.valueOf(i) + ".csv"))) i++;
            fileName = "memory_" + String.valueOf(i) + ".csv";
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            @Override public void run() {
                execute();
                count++;
                if (count >= time) { timer.cancel(); }
            }
        }, 0, 1000);
    }

    private void execute() {
        long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        if(this.mode.equals("LOG")) {
            System.out.println("Total memory: " + totalMemory + " MB");
            System.out.println("Heap memory: " + usedMemory + " MB");
        } else if(this.mode.equals("CSV")) {
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(fileName, true));
                LocalTime time = LocalTime.now();
                writer.println(time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + "," + totalMemory + "," + usedMemory + "");
                writer.close();
                writer.flush();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}