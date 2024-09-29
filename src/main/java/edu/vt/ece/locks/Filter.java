/*
package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

public class Filter implements Lock {
    private final AtomicInteger[] level;
    private final AtomicInteger[] victim;
    private final int numThreads;

    // Default constructor
    public Filter() {
        this(2);
    }

    // Constructor with number of threads parameter
    public Filter(int n) {
        this.numThreads = n;
        level = new AtomicInteger[n];
        victim = new AtomicInteger[n];
        for (int i = 0; i < n; i++) {
            level[i] = new AtomicInteger(0); // Initialize level to 0 for each thread
            victim[i] = new AtomicInteger(-1); // Initialize victim to -1 (no victim initially)
        }
    }

    */
/*@Override
    public void lock() {
        int me = ((ThreadId) Thread.currentThread()).getThreadId();
        for (int i = 1; i < numThreads; i++) { // Loop through levels, starting from 1
            level[me].set(i);
            victim[i].set(me);
            // Wait until there is no thread at the current level or higher with the same victim
            for (int k = 0; k < numThreads; k++) {
                while (k != me && level[k].get() >= i && victim[i].get() == me) {
                    // Busy wait
                }
            }
        }
    }*//*


    */
/*@Override
    public void lock() {
        int me = ((ThreadId)Thread.currentThread()).getThreadId();
        for(int i=1; i<level.length; i++){
            level[me].set(i);
            victim[i].set(me);
            boolean found = false;
            do{
                for(int k=0; k<level.length; k++) {
                    if(k!=me && (found = (level[k].get() >= i && victim[i].get() == me)))
                        break;
                }
            } while(found);
        }
    }*//*


    @Override
    public void lock() {
        int me = ((ThreadId) Thread.currentThread()).getThreadId(); // Get thread ID
        for (int i = 1; i < numThreads; i++) { // Loop through levels, starting from 1
            level[me].set(i); // Set the level for this thread
            victim[i].set(me); // Set this thread as the victim at level i

            // Wait until no thread at the current level or higher is competing
            for (int k = 0; k < numThreads; k++) {
                // Busy-wait condition: if another thread `k` is at level i or higher, and it's not `me`, and it's a victim
                while (k != me && level[k].get() >= i && victim[i].get() == me) {
                    // Busy wait
                    Thread.yield(); // Yield to give other threads a chance
                }
            }
        }
    }


    @Override
    public void unlock() {
        int me = ((ThreadId) Thread.currentThread()).getThreadId();
        level[me].set(0); // Reset level to 0 when unlocking
    }
}
*/

package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

public class Filter implements Lock {
    private final AtomicInteger[] level;
    private final AtomicInteger[] victim;
    private final int numThreads;

    public Filter() {
        this(2);
    }

    // Constructor with number of threads parameter
    public Filter(int n) {
        this.numThreads = n;
        level = new AtomicInteger[n];
        victim = new AtomicInteger[n];
        for (int i = 0; i < n; i++) {
            level[i] = new AtomicInteger(0); // Initialize level to 0 for each thread
            victim[i] = new AtomicInteger(-1); // Initialize victim to -1 (no victim initially)
        }
    }

    @Override
    public void lock() {
        int me = ((ThreadId) Thread.currentThread()).getThreadId(); // Get thread ID
        for (int i = 1; i < numThreads; i++) { // Loop through levels, starting from 1
            level[me].set(i); // Set the level for this thread
            victim[i].set(me); // Set this thread as the victim at level i

            // Wait until no thread at the current level or higher is competing
            for (int k = 0; k < numThreads; k++) {
                if (k == me) continue; // Skip the current thread

                // Busy-wait until no other thread is at the current level or higher, and not marked as a victim
                while (level[k].get() >= i && victim[i].get() == me) {
                    Thread.yield(); // Yield to allow other threads a chance to progress
                }
            }
        }
    }

    @Override
    public void unlock() {
        int me = ((ThreadId) Thread.currentThread()).getThreadId(); // Get thread ID
        level[me].set(0); // Reset level to 0 when unlocking
    }
}

