import java.util.ArrayList;
import java.util.List;

public class MemoryUsageMonitor implements Runnable {
    private List<Long> totalMemoryList;
    private List<Long> heapMemoryList;

    private int timeInSeconds;

    public MemoryUsageMonitor(int timeInSeconds) {
        totalMemoryList = new ArrayList<>();
        heapMemoryList = new ArrayList<>();
        this.timeInSeconds = timeInSeconds;
    }

    @Override
    public void run() {
        startMonitoring(timeInSeconds);
    }

    public void startMonitoring(int durationInSeconds) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationInSeconds * 1000);

        while (System.currentTimeMillis() < endTime) {
            measureMemoryUsage();
            try {
                Thread.sleep(1000); // Warte eine Sekunde zwischen den Messungen
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Ergebnisse anzeigen
        displayResults();
    }

    private void measureMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // Gesamtspeicher in Bytes
        long totalMemory = runtime.totalMemory();
        totalMemoryList.add(totalMemory);

        // Heap-Speicher in Bytes
        long heapMemory = runtime.totalMemory() - runtime.freeMemory();
        heapMemoryList.add(heapMemory);
    }

    private void displayResults() {
        System.out.println("Messergebnisse:");
        System.out.println("Zeit\t\tGesamtspeicher\tHeap-Speicher");

        for (int i = 0; i < totalMemoryList.size(); i++) {
            long time = i + 1;
            long totalMemory = totalMemoryList.get(i);
            long heapMemory = heapMemoryList.get(i);

            System.out.println(time + "s\t\t" + totalMemory + "B\t\t" + heapMemory + "B");
        }
    }
}

