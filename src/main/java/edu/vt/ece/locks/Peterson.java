package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

public class Peterson implements Lock, Comparable<Peterson> {

    private AtomicBoolean[] flag;
    private AtomicInteger victim;
    private int numThreads;

    public Peterson() {
        this(2); // Default to 2 threads for backwards compatibility
    }

    public Peterson(int numThreads) {
        this.numThreads = numThreads;
        flag = new AtomicBoolean[numThreads];
        for (int i = 0; i < numThreads; i++) {
            flag[i] = new AtomicBoolean(false);
        }
        victim = new AtomicInteger();
    }

    @Override
    public void lock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId() % numThreads;
        flag[i].set(true);
        victim.set(i);
        for (int j = 0; j < numThreads; j++) {
            if (j != i) {
                while (flag[j].get() && victim.get() == i) {
                    // Busy wait
                }
            }
        }
    }

    @Override
    public void unlock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId() % numThreads;
        flag[i].set(false);
    }

    @Override
    public int compareTo(Peterson o) {
        return 0;
    }
}