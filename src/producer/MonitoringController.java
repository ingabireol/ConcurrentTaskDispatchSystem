package producer;

import controller.SystemController;
import model.service.MetricsService;
import model.service.QueueService;
import view.console.ConsoleView;
import util.logging.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CONTROLLER: Dedicated monitoring and reporting controller
 */
public class MonitoringController {
    private final MetricsService metricsService;
    private final QueueService queueService;
    private final SystemController systemController;
    private final ConsoleView consoleView;
    private final Logger logger = Logger.getLogger(MonitoringController.class);
    private final ScheduledExecutorService monitorScheduler;
    
    public MonitoringController(MetricsService metricsService,
                               QueueService queueService,
                               SystemController systemController,
                               ConsoleView consoleView) {
        this.metricsService = metricsService;
        this.queueService = queueService;
        this.systemController = systemController;
        this.consoleView = consoleView;
        this.monitorScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Monitor-Thread");
            t.setDaemon(true);
            return t;
        });
        logger.info("Monitoring controller initialized");
    }
    
    public void startMonitoring() {
        // System status monitoring every 5 seconds
        monitorScheduler.scheduleAtFixedRate(this::monitorSystemHealth, 5, 5, TimeUnit.SECONDS);
        
        // Performance metrics every 30 seconds
        monitorScheduler.scheduleAtFixedRate(this::monitorPerformance, 30, 30, TimeUnit.SECONDS);
        
        // Detailed report every 2 minutes
        monitorScheduler.scheduleAtFixedRate(this::generateDetailedReport, 120, 120, TimeUnit.SECONDS);
        
        logger.info("Monitoring started - health check: 5s, performance: 30s, reports: 2m");
    }
    
    public void stopMonitoring() {
        logger.info("Stopping monitoring...");
        monitorScheduler.shutdown();
        try {
            if (!monitorScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                monitorScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Monitoring stopped");
    }
    
    private void monitorSystemHealth() {
        try {
            int queueSize = queueService.size();
            int activeWorkers = systemController.getActiveWorkers();
            
            // Display current status
            consoleView.getStatusDisplayView().displayQueueStatus(queueSize, "PriorityBlockingQueue");
            consoleView.getStatusDisplayView().displayWorkerPoolStatus(activeWorkers, 4); // Assuming 4 workers
            
            // Check for potential issues
            if (queueSize > 50) {
                logger.warn("Queue size growing large: {} tasks", queueSize);
                consoleView.displayError("Queue backlog detected: " + queueSize + " tasks", null);
            }
            
            if (activeWorkers == 0 && queueSize > 0) {
                logger.error("No active workers but {} tasks in queue!", queueSize);
                consoleView.displayError("System stalled - no active workers", null);
            }
            
        } catch (Exception e) {
            logger.error("Error during health monitoring", e);
        }
    }
    
    private void monitorPerformance() {
        try {
            consoleView.getMetricsDisplayView().displayPerformanceMetrics(metricsService.getMetrics());
            metricsService.logCurrentMetrics();
        } catch (Exception e) {
            logger.error("Error during performance monitoring", e);
        }
    }
    
    private void generateDetailedReport() {
        try {
            int queueSize = queueService.size();
            int activeWorkers = systemController.getActiveWorkers();
            
            consoleView.getMetricsDisplayView().displayDetailedReport(
                metricsService.getMetrics(), queueSize, activeWorkers);
                
            logger.info("Detailed system report generated");
        } catch (Exception e) {
            logger.error("Error generating detailed report", e);
        }
    }
}
