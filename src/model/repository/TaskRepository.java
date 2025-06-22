package model.repository;

import model.entity.Task;
import model.entity.TaskStatus;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

/**
 * MODEL: Repository pattern for task data access
 */
public interface TaskRepository {
    void save(Task task, TaskStatus status);
    Optional<Task> findById(UUID taskId);
    TaskStatus getStatus(UUID taskId);
    void updateStatus(UUID taskId, TaskStatus status);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByProducerSource(String producerSource);
    long countByStatus(TaskStatus status);
    void deleteCompleted();
}
