package model.repository;

import model.entity.Task;
import model.entity.TaskStatus;
import util.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MODEL: In-memory implementation of TaskRepository using thread-safe collections
 */
public class InMemoryTaskRepository implements TaskRepository {
    private final ConcurrentHashMap<UUID, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, TaskStatus> statuses = new ConcurrentHashMap<>();
    private final Logger logger = Logger.getLogger(InMemoryTaskRepository.class);
    
    @Override
    public void save(Task task, TaskStatus status) {
        tasks.put(task.getId(), task);
        statuses.put(task.getId(), status);
        logger.debug("Saved task {} with status {}", task.getId(), status);
    }
    
    @Override
    public Optional<Task> findById(UUID taskId) {
        Task task = tasks.get(taskId);
        logger.trace("Looking up task {}: {}", taskId, task != null ? "found" : "not found");
        return Optional.ofNullable(task);
    }
    
    @Override
    public TaskStatus getStatus(UUID taskId) {
        TaskStatus status = statuses.get(taskId);
        logger.trace("Status lookup for task {}: {}", taskId, status);
        return status;
    }
    
    @Override
    public void updateStatus(UUID taskId, TaskStatus status) {
        TaskStatus oldStatus = statuses.put(taskId, status);
        logger.debug("Updated task {} status: {} -> {}", taskId, oldStatus, status);
    }
    
    @Override
    public List<Task> findByStatus(TaskStatus status) {
        List<Task> result = statuses.entrySet().stream()
            .filter(entry -> entry.getValue() == status)
            .map(entry -> tasks.get(entry.getKey()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        logger.trace("Found {} tasks with status {}", result.size(), status);
        return result;
    }
    
    @Override
    public List<Task> findByProducerSource(String producerSource) {
        List<Task> result = tasks.values().stream()
            .filter(task -> task.getProducerSource().equals(producerSource))
            .collect(Collectors.toList());
        
        logger.trace("Found {} tasks from producer {}", result.size(), producerSource);
        return result;
    }
    
    @Override
    public long countByStatus(TaskStatus status) {
        long count = statuses.values().stream()
            .mapToLong(s -> s == status ? 1 : 0)
            .sum();
        
        logger.trace("Count of tasks with status {}: {}", status, count);
        return count;
    }
    
    @Override
    public void deleteCompleted() {
        Set<UUID> completedIds = statuses.entrySet().stream()
            .filter(entry -> entry.getValue().isTerminal())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        
        completedIds.forEach(id -> {
            tasks.remove(id);
            statuses.remove(id);
        });
        
        logger.info("Cleaned up {} completed tasks", completedIds.size());
    }
}