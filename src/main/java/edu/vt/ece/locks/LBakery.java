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
import java.util.concurrent.atomic.AtomicInteger;

public class LBakery implements Lock {
    private final LabelTimestamp[] label;
    private final AtomicInteger currentInCS; // Tracks how many threads are in CS
    private final int L; // Maximum number of threads allowed in the critical section
    private final int n; // Total number of threads

    // Use ThreadLocal to assign unique indices (0 to n-1) to threads
    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private static final ThreadLocal<Integer> threadIndex = ThreadLocal.withInitial(() -> idGenerator.getAndIncrement());

    public LBakery(int l, int n) {
        this.L = l; // L is the maximum number of threads allowed in CS
        this.n = n; // n is the total number of threads
        this.label = new LabelTimestamp[n]; // Array for each thread's timestamp
        this.currentInCS = new AtomicInteger(0); // Tracks how many threads are in CS

        // Initialize all threads' timestamps to 0 (not in CS)
        for (int i = 0; i < n; i++) {
            label[i] = new LabelTimestamp(0, i);
        }
    }

    @Override
    public void lock() {
        int i = threadIndex.get(); // Get the thread's unique index (0 to n-1)
        // Generate a timestamp for this thread
        label[i] = new LabelTimestamp(getNextTicketNumber(), i);

        while (true) {
            int countInCS = 0;
            boolean shouldWait = false;

            for (int k = 0; k < n; k++) {
                if (k == i) continue; // Skip itself

                LabelTimestamp other = label[k];
                if (other.number != 0 && other.compare(label[i])) {
                    countInCS++;
                    if (countInCS >= L || currentInCS.get() >= L) {
                        shouldWait = true;
                        break; // If too many threads are ahead or already in CS, wait
                    }
                }
            }

            if (!shouldWait && currentInCS.get() < L) {
                // Safe to enter the critical section
                currentInCS.incrementAndGet(); // Increment count of threads in CS
                break; // Exit the loop to enter CS
            }

            // Otherwise, spin-wait
            Thread.yield(); // Yield to give other threads a chance
        }
    }

    @Override
    public void unlock() {
        int i = threadIndex.get(); // Use thread's index
        label[i] = new LabelTimestamp(0, i); // Reset timestamp to 0
        currentInCS.decrementAndGet(); // Decrement the count of threads in CS
    }

    private int getNextTicketNumber() {
        int max = 0;
        // Find the maximum ticket number in the system
        for (LabelTimestamp ts : label) {
            if (ts != null) {
                max = Math.max(max, ts.number);
            }
        }
        return max + 1; // Return the next ticket number
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


/*import java.util.concurrent.atomic.AtomicInteger;

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
}*/



