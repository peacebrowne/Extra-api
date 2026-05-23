package com.example.extra.Services.Impl;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.Task;
import com.example.extra.Entities.User;
import com.example.extra.Enumerator.TaskStatus;
import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Exceptions.Custom.NotFound;
import com.example.extra.Mappers.TaskMapper;
import com.example.extra.Mappers.UserMapper;
import com.example.extra.Services.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

      /**
     * Creates a new task and persists it to the database.
     *
     * @param task the Task object to create (should include title, description, clientId, etc.)
     * @return the created Task object with an assigned id
     * @throws InternalServerError if an unexpected error occurs during task creation
     */
    @Override
    @CachePut(value = "tasks", key = "#result.id")
    public Task createTask(Task task) {
        try {

            UserDTO user = userMapper.getUserById(task.getClientId());

            if (user == null) {
                throw new NotFound("User with id " + task.getClientId() + " not found");
            }

            // Create and persist the task to the database
            return taskMapper.createTask(task);

        }catch (NotFound ex){
            log.error(ex.getMessage());
            throw ex;
        }
        catch (Exception e) {
            log.error("Unexpected error occurred while creating task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while creating task");
        }
    }

    /**
     * Updates an existing task with new values.
     *
     * @param task the Task object containing updated field values (id is required)
     * @return the updated Task object
     * @throws NotFound if no task with the given id exists
     * @throws InternalServerError if an unexpected error occurs during update
     *
    */
    @Override
    @CachePut(value = "tasks", key = "#existingTask.id")
    public Task updateTask(Task task) {
        try {
            // Retrieve and verify the task exists
            Task existingTask = getTaskById(task.getId());

            // Update title and description only if provided (non-null)
            Optional.ofNullable(task.getTitle()).ifPresent(existingTask::setTitle);
            Optional.ofNullable(task.getDescription()).ifPresent(existingTask::setDescription);

            // Persist the updated task to the database
            taskMapper.updateTask(task);

            return existingTask;

        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while updating task");
        }
    }

    /**
     * Deletes a task by its unique identifier.
     *
     * @param id the unique identifier (ID) of the task to delete
     * @return the Task object that was deleted
     * @throws NotFound if no task with the given id exists
     * @throws BadRequest if the task cannot be deleted due to invalid state
     * @throws InternalServerError if an unexpected error occurs during deletion
     *
    */
    @Override
    @CacheEvict(value = "task", key = "#id")
    public Task deleteTask(String id) {
        try {
            // Verify the task exists before attempting deletion
            Task existingTask = getTaskById(id);
            taskMapper.deleteTask(id);
            return existingTask;
        }catch (BadRequest | NotFound e) {
            log.error("ERROR: {}", e.getMessage(), e);
            throw e;
        }catch (Exception e) {
            log.error("Unexpected error occurred while deleting task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while deleting task");
        }
    }

    /**
     * Retrieves a single task by its unique identifier.
     *
     * @param id the unique identifier (ID) of the task to retrieve
     * @return the Task object if found
     * @throws NotFound if no task with the given id exists
     * @throws InternalServerError if an unexpected error occurs during retrieval
     *
    */
    @Override
    @Cacheable(value = "task", key = "#id")
    public Task getTaskById(String id) {
        try {
            Task task = taskMapper.getTaskById(id);
            if (task == null) {
                log.warn("Task with id {} not found", id);
                throw new NotFound("Task with id " + id + " not found");
            }
            return task;
        } catch (NotFound e) {
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while retrieving task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while retrieving task");
        }
    }
    /**
     * Retrieves all tasks associated with a given user email.
     *
     * @param email the email address of the user whose tasks are to be retrieved
     * @return a list of all tasks for the specified user; returns an empty list if no tasks exist
     * @throws InternalServerError if an unexpected error occurs during task retrieval
     */
    @Override
    @Cacheable(value = "tasks", key = "#email")
    public List<Task> getAllTasks(String email) {
        try {
            return taskMapper.getAllTasks(email);
        } catch (Exception e) {
            log.error("Unexpected error occurred while retrieving all tasks: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while retrieving all tasks");
        }
    }

    /**
     * Marks a task as accepted by a provider.
     *
     * @param taskId the unique identifier (ID) of the task to accept
     * @param email the unique email of the current logged-in that's accepting the task
     * @return the updated Task object with status changed to "ACCEPTED"
     * @throws NotFound            if no task with the given id exists
     * @throws BadRequest          if the task cannot be accepted (e.g., already accepted or completed)
     * @throws InternalServerError if an unexpected error occurs during acceptance
     */
    @Override
    public Task acceptTask(String taskId, String email) {
        try {

            Task existingTask = getTaskById(taskId);

            String provider = userMapper.getUserByEmail(email).getId();
            if (provider == null) {
                log.warn("User with email {} not found", email);
                throw new NotFound("User with email " + email + " not found");
            }

            if (existingTask.getStatus() != TaskStatus.PENDING) {
                throw new BadRequest("Task has already been accepted");
            }

            if (existingTask.getClientId().equals(provider)) {
                throw new BadRequest("You cannot accept your own task");
            }

            return taskMapper.updateTaskStatus(taskId, provider);

        } catch (NotFound | BadRequest e) {
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while accepting task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while accepting task");
        }
    }


}
