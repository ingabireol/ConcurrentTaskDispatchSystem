package demo;

import util.logging.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DEMO: Additional concurrency pattern demonstrations
 */
public class ConcurrencyDemo {
    private static final Logger logger = Logger.getLogger(ConcurrencyDemo.class);
    
    public static void demonstrateDeadlock() {
        logger.info("Starting deadlock demonstration");
        
        System.out.println("\n⚠️  DEADLOCK DEMONSTRATION");
        System.out.println("===========================");
        
        final Object lock1 = new Object();
        final Object lock2 = new Object();
        
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread-1: Acquired lock1");
                logger.debug("Thread-1 acquired lock1");
                
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                
                System.out.println("Thread-1: Waiting for lock2...");
                synchronized (lock2) {
                    System.out.println("Thread-1: Acquired lock2");
                }
            }
        }, "DeadlockDemo-1");
        
        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread-2: Acquired lock2");
                logger.debug("Thread-2 acquired lock2");
                
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                
                System.out.println("Thread-2: Waiting for lock1...");
                synchronized (lock1) {
                    System.out.println("Thread-2: Acquired lock1");
                }
            }
        }, "DeadlockDemo-2");
        
        thread1.start();
        thread2.start();
        
        // Wait a bit to let deadlock occur
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Force interrupt to break deadlock
        if (thread1.isAlive() || thread2.isAlive()) {
            System.out.println("\n💀 DEADLOCK DETECTED! Interrupting threads...");
            logger.warn("Deadlock detected, interrupting threads");
            thread1.interrupt();
            thread2.interrupt();
        }
        
        try {
            thread1.join(1000);
            thread2.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✅ Deadlock demonstration complete");
        System.out.println("💡 Solution: Always acquire locks in consistent order!");
        System.out.println("===========================\n");
        
        logger.info("Deadlock demonstration completed");
    }
    
    public static void demonstrateSynchronizationPrimitives() {
        logger.info("Demonstrating synchronization primitives");
        
        System.out.println("\n🔧 SYNCHRONIZATION PRIMITIVES DEMO");
        System.out.println("===================================");
        
        demonstrateCountDownLatch();
        demonstrateCyclicBarrier();
        demonstrateReentrantLock();
        
        System.out.println("===================================\n");
    }
    
    private static void demonstrateCountDownLatch() {
        System.out.println("\n🚦 CountDownLatch Example:");
        CountDownLatch latch = new CountDownLatch(3);
        
        for (int i = 1; i <= 3; i++) {
            final int workerId = i;
            new Thread(() -> {
                try {
                    Thread.sleep(1000 + workerId * 500);
                    System.out.printf("   Worker-%d completed initialization%n", workerId);
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "InitWorker-" + i).start();
        }
        
        try {
            System.out.println("   Main thread waiting for all workers to initialize...");
            latch.await();
            System.out.println("   ✅ All workers initialized, system ready!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void demonstrateCyclicBarrier() {
        System.out.println("\n🔄 CyclicBarrier Example:");
        final int PARTY_SIZE = 3;
        CyclicBarrier barrier = new CyclicBarrier(PARTY_SIZE, () -> 
            System.out.println("   🎉 All threads reached barrier - proceeding together!"));
        
        for (int i = 1; i <= PARTY_SIZE; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    System.out.printf("   Thread-%d working on phase 1...%n", threadId);
                    Thread.sleep(1000 + threadId * 300);
                    System.out.printf("   Thread-%d reached barrier%n", threadId);
                    
                    barrier.await(); // Wait for all threads
                    
                    System.out.printf("   Thread-%d proceeding with phase 2%n", threadId);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }, "BarrierThread-" + i).start();
        }
        
        try {
            Thread.sleep(3000); // Let demo complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void demonstrateReentrantLock() {
        System.out.println("\n🔒 ReentrantLock Example:");
        ReentrantLock lock = new ReentrantLock();
        
        Runnable lockTask = () -> {
            String threadName = Thread.currentThread().getName();
            try {
                System.out.printf("   %s attempting to acquire lock...%n", threadName);
                
                if (lock.tryLock(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.printf("   %s acquired lock%n", threadName);
                        Thread.sleep(800);
                        System.out.printf("   %s releasing lock%n", threadName);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.printf("   %s failed to acquire lock within timeout%n", threadName);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        
        Thread t1 = new Thread(lockTask, "LockThread-1");
        Thread t2 = new Thread(lockTask, "LockThread-2");
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("   ✅ ReentrantLock demo completed");
    }
}
