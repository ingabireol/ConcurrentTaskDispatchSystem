package controller;

import model.service.MetricsService;
import model.service.QueueService;
import view.console.ConsoleView;
import view.export.JsonExporter;
import util.logging.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CONTROLLER: System-wide operations and lifecycle management
 */
public class SystemController {
    private final MetricsService metricsService;
    private final QueueService queueService;
    private final ConsoleView consoleView;
    private final JsonExporter jsonExporter;
    private final Logger logger = Logger.getLogger(SystemController.class);
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger activeWorkers = new AtomicInteger(0);
    
    public SystemController(MetricsService metricsService,
                           QueueService queueService,
                           ConsoleView consoleView) {
        this.metricsService = metricsService;
        this.queueService = queueService;
        this.consoleView = consoleView;
        this.jsonExporter = new JsonExporter();
        this.scheduler = Executors.newScheduledThreadPool(2);
        logger.info("System controller initialized");
    }
    
    public void startSystem(int workerThreads, int maxRetries) {
        logger.info("Starting ConcurQueue system with {} workers", workerThreads);
        
        // Display startup in view
        consoleView.displayWelcome();
        consoleView.displaySystemStartup(workerThreads, maxRetries);
        
        // Start monitoring
        startPeriodicMonitoring();
        startPeriodicExport();
        
        logger.info("System startup completed");
    }
    
    public void shutdownSystem() {
        logger.info("Initiating system shutdown");
        
        // Display shutdown in view
        consoleView.displayShutdown();
        
        // Stop monitoring
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Export final metrics
        exportCurrentMetrics("logs/final-metrics.json");
        
        logger.info("System shutdown completed");
    }
    
    private void startPeriodicMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                displaySystemStatus();
            } catch (Exception e) {
                logger.error("Error during periodic monitoring", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        logger.info("Periodic monitoring started (5-second intervals)");
    }
    
    private void startPeriodicExport() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                exportCurrentMetrics("logs/metrics-" + System.currentTimeMillis() + ".json");
            } catch (Exception e) {
                logger.error("Error during periodic export", e);
            }
        }, 60, 60, TimeUnit.SECONDS);
        
        logger.info("Periodic export started (60-second intervals)");
    }
    
    private void displaySystemStatus() {
        int queueSize = queueService.size();
        int workers = activeWorkers.get();
        
        consoleView.getMetricsDisplayView().displaySystemStatus(
            metricsService.getMetrics(), queueSize, workers);
        
        consoleView.getStatusDisplayView().displaySystemHealth("HEALTHY", 
            metricsService.getMetrics().getUptimeSeconds());
    }
    
    private void exportCurrentMetrics(String filename) {
        jsonExporter.exportMetrics(
            metricsService.getMetrics(),
            queueService.size(),
            activeWorkers.get(),
            filename);
    }
    
    public void incrementActiveWorkers() {
        int count = activeWorkers.incrementAndGet();
        logger.debug("Active workers incremented to: {}", count);
    }
    
    public void decrementActiveWorkers() {
        int count = activeWorkers.decrementAndGet();
        logger.debug("Active workers decremented to: {}", count);
    }
    
    public int getActiveWorkers() {
        return activeWorkers.get();
    }
}
