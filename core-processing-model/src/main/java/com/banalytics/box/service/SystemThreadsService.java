package com.banalytics.box.service;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class SystemThreadsService {
    private static final Map<ClassLoader, ForkJoinPool> classLoadersExecutorsMap = new ConcurrentHashMap<>();

    public static ExecutorService getExecutorService(Object o) {
        ClassLoader classLoader =
                o == null ?
                        SystemThreadsService.class.getClassLoader()
                        : o.getClass().getClassLoader();
        return classLoadersExecutorsMap.computeIfAbsent(classLoader, (cl) -> {
            log.info("Class loader registered: {}", cl);
            AppForkJoinWorkerThreadFactory factory = new AppForkJoinWorkerThreadFactory(cl);
            return new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2, factory, (t, e) -> log.error(e.getMessage(), e), false);
        });
    }

    public static void execute(Object context, Runnable r) {
        getExecutorService(context).execute(r);
    }

    public static final Timer SYSTEM_TIMER = new Timer();

    public static final ThreadPoolExecutor STARTUP_EXECUTOR = createSystemTaskExecutor();

    public static final Map<Class, TimeMetric> EXECUTION_METRICS = new HashMap<>();

    public static void reboot() {
        SYSTEM_TIMER.schedule(new TimerTask() {// schedule halt via 15 sec if not exited
            @Override
            public void run() {
                Runtime.getRuntime().halt(0);
            }
        }, 15000);
        Runtime.getRuntime().exit(0);
    }

    private static ThreadPoolExecutor createSystemTaskExecutor() {
        int cpus = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor tp = new ThreadPoolExecutor(
                cpus * 2,
                cpus * 2,
                0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        ) {
            @Override
            public void execute(Runnable command) {
                long st = System.currentTimeMillis();
                super.execute(command);
                long en = System.currentTimeMillis();
                TimeMetric timeMetric = EXECUTION_METRICS.computeIfAbsent(command.getClass(), k -> new TimeMetric(k.getName()));
                timeMetric.push(en - st);
            }
        };
        tp.setRejectedExecutionHandler((r, executor) -> {
            log.warn("Task rejected (tasks / active / pool-size)=({} / {} / {}))\n{}", tp.getTaskCount(), tp.getActiveCount(), tp.getPoolSize(), r);
        });

        return tp;
    }

    static class TimeMetric {
        final String className;
        long counter = 0;
        long average = 0;
        long max = 0;
        long min = 0;

        long[] samples = new long[20];

        public TimeMetric(String className) {
            this.className = className;
        }

        public synchronized void push(long time) {
            counter++;
            max = Math.max(time, max);
            min = Math.min(time, min);

            System.arraycopy(samples, 1, samples, 0, samples.length - 1);
            samples[samples.length - 1] = time;
            long total = 0;
            for (long val : samples) {
                total += val;
            }
            average = total / samples.length;
        }

        @Override
        public String toString() {
            return "TimeMetric{" +
                    "counter=" + counter +
                    ", average=" + average +
                    ", max=" + max +
                    ", min=" + min +
                    ", samples=" + Arrays.toString(samples) +
                    "}\n";
        }
    }
}
