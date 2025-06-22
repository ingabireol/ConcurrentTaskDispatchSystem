package view.console;

import model.entity.Task;
import model.entity.ProcessingResult;
import view.formatter.TaskFormatter;
import util.logging.Logger;
import view.formatter.TaskFormatter;

/**
 * VIEW: Task-specific display formatting and output
 */
public class TaskDisplayView {
    private final Logger logger = Logger.getLogger(TaskDisplayView.class);
    private final TaskFormatter formatter = new TaskFormatter();
    
    public void displayTaskSubmitted(Task task) {
        String formatted = formatter.formatSubmission(task);
        System.out.println("📥 " + formatted);
        logger.info("Task submission displayed: {}", task.getId());
    }
    
    public void displayTaskStarted(Task task) {
        String formatted = formatter.formatProcessingStart(task);
        System.out.println("⚙️  " + formatted);
        logger.info("Task processing started: {}", task.getId());
    }
    
    public void displayTaskCompleted(Task task, ProcessingResult result) {
        String formatted = formatter.formatCompletion(task, result);
        System.out.println("✅ " + formatted);
        logger.info("Task completion displayed: {} - {}", task.getId(), result.isSuccessful() ? "SUCCESS" : "FAILED");
    }
    
    public void displayTaskFailed(Task task, ProcessingResult result) {
        String formatted = formatter.formatFailure(task, result);
        System.out.println("❌ " + formatted);
        logger.warn("Task failure displayed: {} - retry count: {}", task.getId(), task.getRetryCount());
    }
    
    public void displayTaskRetry(Task task) {
        String formatted = formatter.formatRetry(task);
        System.out.println("🔄 " + formatted);
        logger.info("Task retry displayed: {} - attempt {}", task.getId(), task.getRetryCount());
    }
}
