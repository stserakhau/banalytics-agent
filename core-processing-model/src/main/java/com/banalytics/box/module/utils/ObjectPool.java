package com.banalytics.box.module.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Queue<T> objectPool = new LinkedList<>();

    public ObjectPool(int poolSize, Supplier<T> createPooledObjectFunc) {
        for (int i = 0; i < poolSize; i++) {
            objectPool.add(createPooledObjectFunc.get());
        }
    }

    public T acquireObject(){
        synchronized (objectPool) {
            try {
                while (objectPool.isEmpty()) {
                    objectPool.wait();
                }
                return objectPool.remove();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void returnObject(T object) {
        synchronized (objectPool) {
            objectPool.add(object);
            objectPool.notifyAll();
        }
    }

    public int available() {
        return objectPool.size();
    }
}
