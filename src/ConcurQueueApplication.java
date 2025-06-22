

import controller.*;
import model.repository.InMemoryTaskRepository;
import model.repository.TaskRepository;
import model.service.*;
import producer.*;
import producer.MonitoringController;
import view.console.ConsoleView;
import util.threading.WorkerThread;
import util.logging.Logger;
import util.logging.LogLevel;
import demo.RaceConditionDemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MAIN APPLICATION: MVC-based ConcurQueue implementation with comprehensive logging
 */
public class ConcurQueueApplication {
    private static final Logger logger = Logger.getLogger(ConcurQueueApplication.class);
    
    // Configuration
    private static final int WORKER_THREADS = 4;
    private static final int MAX_RETRIES = 3;
    private static final int DEMO_DURATION_SECONDS = 90;
    
    public static void main(String[] args) {
        // Configure logging
        setupLogging();
        
        logger.info("Starting ConcurQueue MVC Application");
        
        try {
            // Demonstrate concurrency concepts first
            demonstrateConcurrencyConcepts();
            
            // Initialize the MVC application
            ConcurQueueApplication app = new ConcurQueueApplication();
            app.run();
            
        } catch (Exception e) {
            logger.fatal("Application failed to start", e);
            System.exit(1);
        }
    }
    
    private static void setupLogging() {
        // Configure different loggers for different components
        Logger.getLogger("TaskController").setThreshold(LogLevel.DEBUG);
        Logger.getLogger("SystemController").setThreshold(LogLevel.INFO);
        Logger.getLogger("QueueService").setThreshold(LogLevel.INFO);
        Logger.getLogger("ProcessingService").setThreshold(LogLevel.INFO);
        Logger.getLogger("MetricsService").setThreshold(LogLevel.DEBUG);
        Logger.getLogger("WorkerThread").setThreshold(LogLevel.INFO);
        Logger.getLogger("TaskProducer").setThreshold(LogLevel.INFO);
        Logger.getLogger("HighPriorityProducer").setThreshold(LogLevel.INFO);
        Logger.getLogger("RegularProducer").setThreshold(LogLevel.DEBUG);
        Logger.getLogger("BackgroundProducer").setThreshold(LogLevel.TRACE);
        
        logger.info("Logging configuration completed");
    }
    
    private static void demonstrateConcurrencyConcepts() {
        logger.info("Demonstrating concurrency concepts...");
        RaceConditionDemo.demonstrateRaceConditions();
        logger.info("Concurrency demonstration completed");
    }
    
    public void run() throws InterruptedException {
        // MODEL Layer - Initialize services and repositories
        TaskRepository taskRepository = new InMemoryTaskRepository();
        QueueService queueService = new QueueService();
        ProcessingService processingService = new ProcessingService(0.15); // 15% failure rate
        MetricsService metricsService = new MetricsService();
        
        // VIEW Layer - Initialize user interface
        ConsoleView consoleView = new ConsoleView();
        
        // CONTROLLER Layer - Initialize controllers
        TaskController taskController = new TaskController(
            queueService, processingService, metricsService, 
            taskRepository, consoleView, MAX_RETRIES);
        
        SystemController systemController = new SystemController(
            metricsService, queueService, consoleView);
        
        MonitoringController monitoringController = new MonitoringController(
            metricsService, queueService, systemController, consoleView);
        
        // Start the system
        systemController.startSystem(WORKER_THREADS, MAX_RETRIES);
        monitoringController.startMonitoring();
        
        // Create and start worker threads
        ExecutorService workerPool = Executors.newFixedThreadPool(WORKER_THREADS);
        for (int i = 1; i <= WORKER_THREADS; i++) {
            workerPool.submit(new WorkerThread(taskController, systemController, i));
        }
        
        // Create and start producer threads
        TaskProducer[] producers = {
            new HighPriorityProducer(taskController),
            new RegularProducer(taskController),
            new BackgroundProducer(taskController)
        };
        
        ExecutorService producerPool = Executors.newFixedThreadPool(producers.length);
        for (TaskProducer producer : producers) {
            producerPool.submit(producer);
            consoleView.getStatusDisplayView().displayProducerStarted(producer.getName());
        }
        
        logger.info("All system components started successfully");
        
        // Let the system run for the demo duration
        Thread.sleep(DEMO_DURATION_SECONDS * 1000);
        
        // Graceful shutdown
        shutdown(systemController, monitoringController, workerPool, producerPool, producers, consoleView);
    }
    
    private void shutdown(SystemController systemController,
                         MonitoringController monitoringController,
                         ExecutorService workerPool,
                         ExecutorService producerPool,
                         TaskProducer[] producers,
                         ConsoleView consoleView) {
        
        logger.info("Initiating graceful shutdown...");
        
        // Stop producers first
        for (TaskProducer producer : producers) {
            producer.stop();
            consoleView.getStatusDisplayView().displayProducerStopped(producer.getName());
        }
        producerPool.shutdown();
        
        // Stop monitoring
        monitoringController.stopMonitoring();
        
        // Shutdown worker pool gracefully
        workerPool.shutdown();
        
        try {
            // Wait for existing tasks to complete
            if (!workerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Force shutting down worker threads...");
                workerPool.shutdownNow();
                
                if (!workerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.error("Some worker threads did not terminate gracefully");
                }
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Final system shutdown
        systemController.shutdownSystem();
        
        // Close logging resources
        util.logging.FileLogger.closeAll();
        
        logger.info("Application shutdown completed successfully");
        System.out.println("\n👋 ConcurQueue MVC Application terminated gracefully!");
    }
}
