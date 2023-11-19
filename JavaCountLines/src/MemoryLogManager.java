import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.time.LocalDateTime;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

public class MemoryLogManager{

    private int logCount;
    private Timer timer;
    private BufferedWriter writer;
    private boolean silent;


    public MemoryLogManager(int logCount, boolean silent) {
        this.logCount = logCount;
        this.silent = silent;
    }

    public void start(){
        try {
            writer = new BufferedWriter(new FileWriter(new File("memoryLog_"+ LocalDateTime.now() +".csv")));
            writer.write("Gesammtbelegung,\"Freier Speicher\", \"Heap Speicher\"\n");
        
        } catch (IOException e){
            System.out.println(e);
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logMemory();
            }
        }, 0, 1000);
    }

    private void logMemory(){
        logCount--;
        if (logCount < 0){
            timer.cancel();

            try {
                writer.close();
            } catch (IOException e){
                System.out.println(e);
            }
            System.out.println("Timer Finished");
            return;
        }

        long totalMem = Runtime.getRuntime().totalMemory() / 1024;
        long freeMem = Runtime.getRuntime().freeMemory() / 1024;
        long usedMem = totalMem - freeMem;

        if (!silent)
            System.out.println("Total Memory: " + totalMem + " KB\n" +
                "Free Memory: " + freeMem + " KB\n" +
                "Used Memory: " + usedMem + " KB\n"
            );
       
        try {
            writer.write(totalMem+","+freeMem+","+usedMem+System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}