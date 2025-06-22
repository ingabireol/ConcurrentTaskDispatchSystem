package view.console;

import util.logging.Logger;

/**
 * VIEW: Main console interface for user interaction
 */
public class ConsoleView {
    private final Logger logger = Logger.getLogger(ConsoleView.class);
    private final TaskDisplayView taskDisplayView;
    private final MetricsDisplayView metricsDisplayView;
    private final StatusDisplayView statusDisplayView;
    
    public ConsoleView() {
        this.taskDisplayView = new TaskDisplayView();
        this.metricsDisplayView = new MetricsDisplayView();
        this.statusDisplayView = new StatusDisplayView();
        logger.info("Console view initialized");
    }
    
    public void displayWelcome() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    🚀 ConcurQueue System                     ║");
        System.out.println("║              Concurrent Task Dispatch Platform              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        logger.info("Welcome message displayed");
    }
    
    public void displaySystemStartup(int workerThreads, int maxRetries) {
        System.out.println("🔧 System Configuration:");
        System.out.println("   • Worker Threads: " + workerThreads);
        System.out.println("   • Max Retries: " + maxRetries);
        System.out.println("   • Monitoring: Enabled");
        System.out.println("   • Logging: Multi-level");
        System.out.println();
        logger.info("System startup configuration displayed - workers: {}, retries: {}", 
                   workerThreads, maxRetries);
    }
    
    public void displayShutdown() {
        System.out.println("\n🛑 System Shutdown Initiated");
        System.out.println("   • Stopping producers...");
        System.out.println("   • Draining task queue...");
        System.out.println("   • Waiting for workers to complete...");
        System.out.println("   • Generating final reports...");
        logger.info("Shutdown sequence displayed");
    }
    
    public void displayError(String message, Exception e) {
        System.err.println("❌ ERROR: " + message);
        if (e != null) {
            System.err.println("   Details: " + e.getMessage());
        }
        logger.error("Error displayed: {}", message, e);
    }
    
    public TaskDisplayView getTaskDisplayView() { return taskDisplayView; }
    public MetricsDisplayView getMetricsDisplayView() { return metricsDisplayView; }
    public StatusDisplayView getStatusDisplayView() { return statusDisplayView; }
}