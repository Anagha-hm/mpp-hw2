package edu.vt.ece.locks;

/*public class LBakery implements Lock {

    public LBakery(int l, int n) {

    }

    @Override
    public void lock() {

    }

    @Override
    public void unlock() {

    }
}*/
/*import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class LBakery implements Lock {
    private final int L;
    private final int n;
    private final ConcurrentHashMap<Integer, Timestamp> label;
    private final AtomicInteger nextNumber;
    private final Semaphore criticalSectionSemaphore;

    public LBakery(int L, int n) {
        this.L = L;
        this.n = n;
        this.label = new ConcurrentHashMap<>();
        this.nextNumber = new AtomicInteger(1);
        this.criticalSectionSemaphore = new Semaphore(L, true);
    }

    @Override
    public void lock() {
        Thread thread = Thread.currentThread();
        int i = (int) thread.threadId();
        Timestamp myTimestamp = getTimestamp(i);
        label.put(i, myTimestamp);

        try {
            criticalSectionSemaphore.acquire();

            for (Integer k : label.keySet()) {
                if (k == i) continue;
                Timestamp otherTimestamp;
                while ((otherTimestamp = label.get(k)) != null &&
                        ((LabelTimestamp) otherTimestamp).number != 0 &&
                        otherTimestamp.compare(myTimestamp)) {
                    // Spin-wait
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void unlock() {
        Thread thread = Thread.currentThread();
        int i = (int) thread.threadId();
        label.put(i, new LabelTimestamp(0, i));
        criticalSectionSemaphore.release();
    }

    private Timestamp getTimestamp(int threadId) {
        int max = label.values().stream()
                .mapToInt(ts -> ((LabelTimestamp) ts).number)
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
}*/ // doesnt work - infinite loop mostly


/*import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LBakery implements Lock {
    private final ConcurrentHashMap<Integer, Timestamp> label;
    private final AtomicInteger nextNumber;
    private final AtomicInteger currentInCS;
    private final int L; // Maximum number of threads allowed in the critical section

    public LBakery(int l, int n) {
        this.L = l; // L is the maximum number of threads allowed
        this.label = new ConcurrentHashMap<>();
        this.nextNumber = new AtomicInteger(1);
        this.currentInCS = new AtomicInteger(0); // Tracks how many threads are in CS
    }

    @Override
    public void lock() {
        Thread thread = Thread.currentThread();
        int i = (int) thread.threadId();
        Timestamp myTimestamp = getTimestamp(i);
        label.put(i, myTimestamp);

        while (true) {
            int count = 0;
            for (Integer k : label.keySet()) {
                if (k == i) continue;
                Timestamp otherTimestamp = label.get(k);
                if (otherTimestamp != null && ((LabelTimestamp)otherTimestamp).number != 0 &&
                        otherTimestamp.compare(myTimestamp)) {
                    count++;
                    if (count >= L) {
                        break;
                    }
                }
            }
            if (count < L && currentInCS.get() < L) {
                break; // Allowed to enter critical section
            }
            // Spin-wait
        }
        currentInCS.incrementAndGet(); // Increment the count of threads in CS
    }

    @Override
    public void unlock() {
        Thread thread = Thread.currentThread();
        int i = (int) thread.threadId();
        label.put(i, new LabelTimestamp(0, i));
        currentInCS.decrementAndGet(); // Decrement the count of threads in CS
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
}*/ // works - but doesnt use n

import java.util.concurrent.atomic.AtomicInteger;

public class LBakery implements Lock {
    private final LabelTimestamp[] label;
    private final AtomicInteger nextNumber;
    private final AtomicInteger currentInCS;
    private final int L; // Maximum number of threads allowed in the critical section
    private final int n; // Total number of threads

    // Use ThreadLocal to assign unique indices (0 to n-1) to threads
    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private static final ThreadLocal<Integer> threadIndex = ThreadLocal.withInitial(() -> idGenerator.getAndIncrement());

    public LBakery(int l, int n) {
        this.L = l; // L is the maximum number of threads allowed in CS
        this.n = n; // n is the total number of threads
        this.label = new LabelTimestamp[n]; // Array for each thread's timestamp
        this.nextNumber = new AtomicInteger(1);
        this.currentInCS = new AtomicInteger(0); // Tracks how many threads are in CS

        // Initialize all threads' timestamps to 0 (not in CS)
        for (int i = 0; i < n; i++) {
            label[i] = new LabelTimestamp(0, i);
        }
    }

    @Override
    public void lock() {
        int i = threadIndex.get(); // Get the thread's unique index (0 to n-1)
        Timestamp myTimestamp = getTimestamp(i);
        label[i] = (LabelTimestamp) myTimestamp; // Assign the thread's timestamp

        while (true) {
            boolean allowedInCS = true;
            int count = 0;

            for (int k = 0; k < n; k++) { // Loop over all threads
                if (k == i) continue; // Skip itself
                LabelTimestamp otherTimestamp = label[k];

                if (otherTimestamp != null && otherTimestamp.number != 0 &&
                        otherTimestamp.compare(myTimestamp)) {
                    count++;
                    if (count >= L || currentInCS.get() >= L) {
                        allowedInCS = false; // If L threads are in the CS, wait
                        break;
                    }
                }
            }

            // Only enter CS if fewer than L threads are in the critical section
            if (allowedInCS && currentInCS.get() < L) {
                currentInCS.incrementAndGet(); // Increment the count of threads in CS
                break;
            }

            // Spin-wait until fewer than L threads are ahead and in CS
        }
    }

    @Override
    public void unlock() {
        int i = threadIndex.get(); // Use thread's index
        label[i] = new LabelTimestamp(0, i); // Reset timestamp to 0
        currentInCS.decrementAndGet(); // Decrement the count of threads in CS
    }

    private Timestamp getTimestamp(int threadId) {
        int max = 0;
        // Find the maximum ticket number in the system
        for (LabelTimestamp ts : label) {
            if (ts != null) {
                max = Math.max(max, ts.number);
            }
        }
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



