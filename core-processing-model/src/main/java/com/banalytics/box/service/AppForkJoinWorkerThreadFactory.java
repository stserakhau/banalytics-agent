package com.banalytics.box.service;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

@RequiredArgsConstructor
public class AppForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    private final ClassLoader classLoader;

    @Override
    public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new AppForkJoinWorkerThread(pool, classLoader);
    }

    private static class AppForkJoinWorkerThread extends ForkJoinWorkerThread {
        private AppForkJoinWorkerThread(ForkJoinPool pool, ClassLoader classLoader) {
            super(pool);
            setContextClassLoader(classLoader);
        }
    }
}