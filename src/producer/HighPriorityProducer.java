package producer;

import controller.TaskController;
import model.entity.Task;

import java.util.concurrent.ThreadLocalRandom;

/**
 * PRODUCER: Generates high-priority urgent tasks
 */
public class HighPriorityProducer extends TaskProducer {
    
    public HighPriorityProducer(TaskController taskController) {
        super(taskController, "HighPriority-Producer");
    }
    
    @Override
    protected void generateTasks() {
        // Generate 1-2 high priority tasks
        int numTasks = ThreadLocalRandom.current().nextInt(1, 3);
        
        for (int i = 0; i < numTasks; i++) {
            int taskNum = taskCounter.get() + 1;
            int priority = ThreadLocalRandom.current().nextInt(8, 11); // Priority 8-10
            
            String taskName = String.format("%s-URGENT-%d", producerName, taskNum);
            String payload = String.format("CRITICAL: Emergency processing required for operation #%d", taskNum);
            
            Task task = createTask(taskName, priority, payload);
            submitTask(task);
            
            logger.info("Generated high priority task: {} (Priority: {})", taskName, priority);
        }
    }
    
    @Override
    protected int getSubmissionIntervalMs() {
        // Submit every 2-4 seconds (urgent tasks arrive frequently)
        return ThreadLocalRandom.current().nextInt(2000, 4000);
    }
}