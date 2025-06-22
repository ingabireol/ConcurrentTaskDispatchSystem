package view.console;

import model.entity.SystemMetrics;
import view.formatter.MetricsFormatter;
import util.logging.Logger;

/**
 * VIEW: System metrics display and formatting
 */
public class MetricsDisplayView {
    private final Logger logger = Logger.getLogger(MetricsDisplayView.class);
    private final MetricsFormatter formatter = new MetricsFormatter();
    
    public void displaySystemStatus(SystemMetrics metrics, int queueSize, int activeThreads) {
        System.out.println("\n" + formatter.formatSystemStatus(metrics, queueSize, activeThreads));
        logger.debug("System status displayed - queue: {}, active: {}", queueSize, activeThreads);
    }
    
    public void displayPerformanceMetrics(SystemMetrics metrics) {
        System.out.println("\n" + formatter.formatPerformanceMetrics(metrics));
        logger.debug("Performance metrics displayed");
    }
    
    public void displayDetailedReport(SystemMetrics metrics, int queueSize, int activeThreads) {
        System.out.println("\n" + formatter.formatDetailedReport(metrics, queueSize, activeThreads));
        logger.info("Detailed report displayed");
    }
}
