package edu.fool.threadpool;


import edu.fool.component.BlockingQueue;

@FunctionalInterface
public interface RejectPolicy<T> {
    void reject(BlockingQueue<T> queue, T task);
}
