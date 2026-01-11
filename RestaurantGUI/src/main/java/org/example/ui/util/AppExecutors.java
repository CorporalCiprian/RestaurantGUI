package org.example.ui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class AppExecutors {

    private AppExecutors() {
    }

    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            new DaemonThreadFactory("db-worker-")
    );

    public static ExecutorService db() {
        return DB_EXECUTOR;
    }

    public static void shutdownBestEffort() {
        DB_EXECUTOR.shutdown();
        try {
            if (!DB_EXECUTOR.awaitTermination(2, TimeUnit.SECONDS)) {
                DB_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DB_EXECUTOR.shutdownNow();
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private final String prefix;
        private int idx = 1;

        private DaemonThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public synchronized Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + idx++);
            t.setDaemon(true);
            return t;
        }
    }
}
