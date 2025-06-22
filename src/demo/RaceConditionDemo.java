package demo;

import util.logging.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * DEMO: Demonstrates race conditions and their solutions
 */
public class RaceConditionDemo {
    private static final Logger logger = Logger.getLogger(RaceConditionDemo.class);
    
    // Unsafe counter - will show race conditions
    private static volatile int unsafeCounter = 0;
    
    // Safe counter using AtomicInteger
    private static final AtomicInteger safeCounter = new AtomicInteger(0);
    
    // Safe counter using synchronization
    private static volatile int synchronizedCounter = 0;
    
    public static void demonstrateRaceConditions() {
        logger.info("Starting race condition demonstration");
        
        System.out.println("\n🔬 RACE CONDITION DEMONSTRATION");
        System.out.println("==================================");
        
        final int NUM_THREADS = 5;
        final int INCREMENTS_PER_THREAD = 1000;
        final int EXPECTED_TOTAL = NUM_THREADS * INCREMENTS_PER_THREAD;
        
        Thread[] threads = new Thread[NUM_THREADS];
        
        // Reset counters
        unsafeCounter = 0;
        safeCounter.set(0);
        synchronizedCounter = 0;
        
        System.out.printf("Creating %d threads, each incrementing %d times%n", 
                         NUM_THREADS, INCREMENTS_PER_THREAD);
        System.out.printf("Expected final value: %d%n%n", EXPECTED_TOTAL);
        
        logger.info("Creating {} threads with {} increments each", NUM_THREADS, INCREMENTS_PER_THREAD);
        
        // Create and start threads
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                logger.debug("Thread-{} started", threadId);
                
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    // Unsafe increment - race condition!
                    unsafeCounter++; // NOT THREAD SAFE
                    
                    // Safe increment using AtomicInteger
                    safeCounter.incrementAndGet(); // THREAD SAFE
                    
                    // Safe increment using synchronization
                    incrementSynchronizedCounter(); // THREAD SAFE
                }
                
                System.out.printf("✓ Thread-%d completed%n", threadId);
                logger.debug("Thread-{} completed", threadId);
            }, "RaceDemo-" + i);
            
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread join interrupted", e);
                return;
            }
        }
        
        // Show results
        System.out.println("\n📊 RESULTS:");
        
        boolean unsafePassed = unsafeCounter == EXPECTED_TOTAL;
        boolean atomicPassed = safeCounter.get() == EXPECTED_TOTAL;
        boolean syncPassed = synchronizedCounter == EXPECTED_TOTAL;
        
        System.out.printf("❌ Unsafe Counter:        %,7d (Expected: %,7d) - %s%n", 
                         unsafeCounter, EXPECTED_TOTAL, 
                         unsafePassed ? "✅ CORRECT" : "❌ RACE CONDITION!");
        
        System.out.printf("✅ Atomic Counter:        %,7d (Expected: %,7d) - %s%n", 
                         safeCounter.get(), EXPECTED_TOTAL, 
                         atomicPassed ? "✅ CORRECT" : "❌ ERROR!");
        
        System.out.printf("✅ Synchronized Counter:  %,7d (Expected: %,7d) - %s%n", 
                         synchronizedCounter, EXPECTED_TOTAL, 
                         syncPassed ? "✅ CORRECT" : "❌ ERROR!");
        
        System.out.println("\n💡 ANALYSIS:");
        System.out.println("   • Unsafe counter shows lost updates due to race conditions");
        System.out.println("   • Multiple threads read same value before incrementing");
        System.out.println("   • AtomicInteger provides lock-free thread safety");
        System.out.println("   • Synchronized methods ensure mutual exclusion");
        System.out.println("==================================\n");
        
        logger.info("Race condition demo completed - Unsafe: {}, Atomic: {}, Sync: {}", 
                   unsafeCounter, safeCounter.get(), synchronizedCounter);
    }
    
    private static synchronized void incrementSynchronizedCounter() {
        synchronizedCounter++;
    }
}
