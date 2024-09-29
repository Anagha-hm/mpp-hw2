package edu.vt.ece.locks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Bakery implements Lock {
    private final ConcurrentHashMap<Integer, Timestamp> label;
    private final AtomicInteger nextNumber;

    public Bakery() {
        this(2);
    }

    public Bakery(int n) {
        this.label = new ConcurrentHashMap<>();
        this.nextNumber = new AtomicInteger(1);
    }

    @Override
    public void lock() {
        Thread thread = Thread.currentThread();
        int i = (int) thread.getId();
        Timestamp myTimestamp = getTimestamp(i);
        label.put(i, myTimestamp);

        for (Integer k : label.keySet()) {
            if (k == i) continue;
            Timestamp otherTimestamp;
            while ((otherTimestamp = label.get(k)) != null &&
                    ((LabelTimestamp)otherTimestamp).number != 0 &&
                    otherTimestamp.compare(myTimestamp)) {
                // Spin-wait
            }
        }
    }

    @Override
    public void unlock() {
        Thread thread = Thread.currentThread();
        int i = (int) thread.getId();
        label.put(i, new LabelTimestamp(0, i));
    }

    private Timestamp getTimestamp(int threadId) {
        int max = label.values().stream()
                .mapToInt(ts -> ((LabelTimestamp)ts).number)
                .max()
                .orElse(0);
        return new LabelTimestamp(nextNumber.getAndIncrement(), threadId);
    }

    private static class LabelTimestamp implements Timestamp {
        final int number;
        final int threadId;

        LabelTimestamp(int number, int threadId) {
            this.number = number;
            this.threadId = threadId;
        }

        @Override
        public boolean compare(Timestamp other) {
            if (!(other instanceof LabelTimestamp)) {
                throw new IllegalArgumentException("Can only compare LabelTimestamps");
            }
            LabelTimestamp otherTimestamp = (LabelTimestamp) other;
            return (this.number < otherTimestamp.number) ||
                    (this.number == otherTimestamp.number && this.threadId < otherTimestamp.threadId);
        }
    }
}
//// Assume ThreadID class is implemented elsewhere
//class ThreadID {
//    private static final AtomicInteger nextID = new AtomicInteger(0);
//    private static final ThreadLocal<Integer> threadID = ThreadLocal.withInitial(() -> nextID.getAndIncrement());
//
//    public static int get() {
//        return threadID.get();
//    }
//}
//
//interface Lock {
//    void lock();
//    void unlock();
//}