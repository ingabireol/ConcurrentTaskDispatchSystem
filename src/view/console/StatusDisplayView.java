package view.console;


import util.logging.Logger;

/**
 * VIEW: System status and health display
 */
public class StatusDisplayView {
    private final Logger logger = Logger.getLogger(StatusDisplayView.class);
    
    public void displayProducerStarted(String producerName) {
        System.out.println("🏭 Producer Started: " + producerName);
        logger.info("Producer started: {}", producerName);
    }
    
    public void displayProducerStopped(String producerName) {
        System.out.println("🛑 Producer Stopped: " + producerName);
        logger.info("Producer stopped: {}", producerName);
    }
    
    public void displayWorkerPoolStatus(int active, int total) {
        System.out.printf("👷 Worker Pool: %d/%d active threads%n", active, total);
        logger.debug("Worker pool status: {}/{}", active, total);
    }
    
    public void displayQueueStatus(int size, String queueType) {
        System.out.printf("📦 Queue Status: %d tasks pending (%s)%n", size, queueType);
        logger.debug("Queue status: {} tasks in {}", size, queueType);
    }
    
    public void displaySystemHealth(String status, long uptimeSeconds) {
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        System.out.printf("💚 System Health: %s (Uptime: %02d:%02d:%02d)%n", 
                         status, hours, minutes, seconds);
        logger.info("System health: {} - uptime: {}s", status, uptimeSeconds);
    }
}
