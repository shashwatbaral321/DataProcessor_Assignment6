import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class DataProcessor {
    private static final int NUM_WORKERS = 4;
    private static final int NUM_TASKS = 10;

    private static final BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();
    private static final List<String> results = Collections.synchronizedList(new ArrayList<>());
    private static final Logger logger = Logger.getLogger(DataProcessor.class.getName());

    public static void main(String[] args) {
        setupConsoleLogger(); // only console logging

        for (int i = 1; i <= NUM_TASKS; i++) {
            taskQueue.add("Task-" + i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS);
        for (int i = 0; i < NUM_WORKERS; i++) {
            executor.execute(new Worker(i));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Executor interrupted", e);
        }

        writeResults(); // fallback to console if file fails
    }

    private static void setupConsoleLogger() {
        logger.setUseParentHandlers(false); // disable default
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }

    private static void writeResults() {
        try (PrintWriter out = new PrintWriter("results_java.txt")) {
            for (String result : results) {
                out.println(result);
            }
            logger.info("Results written to results_java.txt");
        } catch (IOException e) {
            logger.severe("Could not write to file. Printing results instead:");
            for (String result : results) {
                System.out.println(result);
            }
        }
    }

    static class Worker implements Runnable {
        private final int workerId;

        Worker(int id) {
            this.workerId = id;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String task = taskQueue.poll(1, TimeUnit.SECONDS);
                    if (task == null) break;

                    logger.info("Worker " + workerId + " started " + task);
                    processTask(task);
                    logger.info("Worker " + workerId + " completed " + task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("Worker " + workerId + " interrupted");
                    break;
                } catch (Exception e) {
                    logger.severe("Worker " + workerId + " error: " + e.getMessage());
                }
            }
        }

        private void processTask(String task) throws InterruptedException {
            Thread.sleep(500);
            results.add("Result of " + task + " by Worker-" + workerId);
        }
    }
}
