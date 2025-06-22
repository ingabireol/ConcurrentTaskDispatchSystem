package producer;

import controller.TaskController;
import model.entity.Task;

import java.util.concurrent.ThreadLocalRandom;

/**
 * PRODUCER: Generates low-priority background batch tasks
 */
public class BackgroundProducer extends TaskProducer {
    
    public BackgroundProducer(TaskController taskController) {
        super(taskController, "Background-Producer");
    }
    
    @Override
    protected void generateTasks() {
        // Generate 3-6 background tasks
        int numTasks = ThreadLocalRandom.current().nextInt(3, 7);
        
        for (int i = 0; i < numTasks; i++) {
            int taskNum =  taskCounter.get() + 1;
            int priority = ThreadLocalRandom.current().nextInt(1, 4); // Priority 1-3
            
            String taskName = String.format("%s-BATCH-%d", producerName, taskNum);
            String payload = String.format("Background batch job #%d - maintenance/cleanup operations", taskNum);
            
            Task task = createTask(taskName, priority, payload);
            submitTask(task);
            
            logger.trace("Generated background task: {} (Priority: {})", taskName, priority);
        }
    }
    
    @Override
    protected int getSubmissionIntervalMs() {
        // Submit every 5-8 seconds (background processing is less frequent)
        return ThreadLocalRandom.current().nextInt(5000, 8000);
    }
}