package view.export;

import model.entity.SystemMetrics;
import util.logging.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

/**
 * VIEW: Exports system data to JSON format
 */
public class JsonExporter {
    private final Logger logger = Logger.getLogger(JsonExporter.class);
    
    public void exportMetrics(SystemMetrics metrics, int queueSize, int activeThreads, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{\n");
            writer.write("  \"timestamp\": \"" + Instant.now() + "\",\n");
            writer.write("  \"systemMetrics\": {\n");
            writer.write("    \"tasksSubmitted\": " + metrics.getTasksSubmitted() + ",\n");
            writer.write("    \"tasksCompleted\": " + metrics.getTasksCompleted() + ",\n");
            writer.write("    \"tasksFailed\": " + metrics.getTasksFailed() + ",\n");
            writer.write("    \"tasksInProgress\": " + metrics.getTasksInProgress() + ",\n");
            writer.write("    \"totalProcessingTime\": " + metrics.getTotalProcessingTime() + ",\n");
            writer.write("    \"averageProcessingTime\": " + metrics.getAverageProcessingTime() + ",\n");
            writer.write("    \"completionRate\": " + metrics.getCompletionRate() + ",\n");
            writer.write("    \"uptimeSeconds\": " + metrics.getUptimeSeconds() + "\n");
            writer.write("  },\n");
            writer.write("  \"currentState\": {\n");
            writer.write("    \"queueSize\": " + queueSize + ",\n");
            writer.write("    \"activeThreads\": " + activeThreads + "\n");
            writer.write("  }\n");
            writer.write("}\n");
            
            logger.info("Metrics exported to JSON file: {}", filename);
            
        } catch (IOException e) {
            logger.error("Failed to export metrics to JSON", e);
        }
    }
}
