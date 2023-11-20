import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MemMonitor {
    public static void main(String[] args) {
        /* args:
         * 0: file to write stats to
         * 1: name of class to test
         * ... arguments for test class
         */
        try (FileWriter output = new FileWriter(args[0])) {
            Method main = Class.forName(args[1]).getMethod("main", String[].class);
            String[] params = Arrays.copyOfRange(args, 2, args.length);
            TestProgram(main, params, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void TestProgram(Method main, String[] params, FileWriter output)
            throws IOException, InvocationTargetException, IllegalAccessException {
        var timer = new Timer();
        var monitor = new JVMMemoryMonitor(output);
        timer.scheduleAtFixedRate(monitor, 0, 1000 / 100); //we take 100 subsamples to get a better picture
        long timeStart = System.currentTimeMillis();
        main.invoke(null, (Object)params);
        long timeEnd = System.currentTimeMillis();
        timer.cancel();
        output.write(String.format("\n%d\n", timeEnd - timeStart));
        System.gc();
    }

    private static class JVMMemoryMonitor extends TimerTask {
        private final MemoryMXBean memoryBean;
        private final FileWriter output;
        private long tick = 0;
        private long maxHeap = 0;
        private long maxTotal = 0;

        public JVMMemoryMonitor(FileWriter output) {
            memoryBean = ManagementFactory.getMemoryMXBean();
            this.output = output;
        }

        @Override public void run() {
            long usedHeapMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long usedTotalMemory = usedHeapMemory + memoryBean.getNonHeapMemoryUsage().getUsed();
            maxHeap = Math.max(maxHeap, usedHeapMemory);
            maxTotal = Math.max(maxTotal, usedTotalMemory);
            if (tick % 100 == 0) {
                try {
                    output.write(String.format("%d,%d ", maxHeap, maxTotal));
                } catch (IOException ignored) { }
                maxHeap = 0;
                maxTotal = 0;
            }
            ++tick;
        }
    }
}
