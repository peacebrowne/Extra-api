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


    /**
     * Creates a new task and persists it to the database.
     *
     * @param task the Task object to create (should include title, description, clientId, etc.)
     * @return the created Task object with an assigned id
     * @throws InternalServerError if an unexpected error occurs during task creation
     */
    @Override
    @CachePut(value = "task", key = "#result.id")
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
    @CachePut(value = "task", key = "#existingTask.id")
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

            log.info("Getting task with id {}", id);

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
     * @param id the unique identifier (ID) of the task to accept
     * @param email the unique email of the current logged-in that's accepting the task
     * @return the updated Task object with status changed to "ACCEPTED"
     * @throws NotFound            if no task with the given id exists
     * @throws BadRequest          if the task cannot be accepted (e.g., already accepted or completed)
     * @throws InternalServerError if an unexpected error occurs during acceptance
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task acceptTask(String id, String email) {
        try {

            Task existingTask = getTaskById(id);

            User user = userMapper.getUserByEmail(email);
            if (user == null) {
                throw new NotFound("User with email " + email + " not found");
            }

            String providerId = getProviderId(user, existingTask);

            Task updatedTask = taskMapper.updateStatusToAccepted(id, providerId);

            if (updatedTask == null) {
                throw new BadRequest("Task was already accepted by another provider");
            }

            return updatedTask;

        } catch (NotFound | BadRequest e) {
            log.error("Accepting Task Validation Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while accepting task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while accepting task");
        }
    }


    /**
     * Transitions a task from ACCEPTED to IN_PROGRESS when the provider begins work.
     *
     * @param id the unique identifier (ID) of the task to start
     * @param email the email address of the provider starting the task
     * @return the updated Task object with status changed to "IN_PROGRESS"
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task status is not ACCEPTED or if the user is not the assigned
     *                    provider (not authorized to start this task)
     * @throws InternalServerError if an unexpected error occurs during the operation
     *
    */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task startTask(String id, String email) {

        try {
            // Retrieve and verify the task exists
            Task existingTask = getTaskById(id);

            // Retrieve and verify the provider exists
            User user = userMapper.getUserByEmail(email);
            if (user == null) {
                throw new NotFound("User not found");
            }
            String providerId = user.getId();

            // Validate task status is ACCEPTED
            if (existingTask.getStatus() != TaskStatus.ACCEPTED) {
                throw new BadRequest("Task cannot be started unless it is in ACCEPTED status.");
            }

            // Validate the user is the assigned provider
            if (existingTask.getProviderId() == null || !existingTask.getProviderId().equals(providerId)) {
                throw new BadRequest("You are not authorized to start this task.");
            }

            // Update task status to IN_PROGRESS
            Task updatedTask = taskMapper.updateStatusToInProgress(id, providerId);
            if (updatedTask == null) {
                throw new BadRequest("Failed to start task. State may have changed.");
            }

            return updatedTask;

        } catch (NotFound | BadRequest e) {
            log.error("Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error starting task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while starting the task");
        }
    }

    /**
     * Marks a task as pending confirmation after provider work completion
     * and await the client's confirmation.
     *
     * @param id the unique identifier (ID) of the task
     * @param email the email address of the provider submitting the task for confirmation
     * @return the updated Task object with status changed to "PENDING_CONFIRMATION"
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task status is not IN_PROGRESS or if the user is not the assigned
     *                    provider (not authorized to submit for confirmation)
     * @throws InternalServerError if an unexpected error occurs during the operation
    */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task taskPendingConfirmation(String id, String email) {
        try {
            Task existingTask = getTaskById(id);

            User user = userMapper.getUserByEmail(email);
            if (user == null) {
                throw new NotFound("User with email " + email + " not found");
            }
            String providerId = user.getId();

            if (existingTask.getStatus() != TaskStatus.IN_PROGRESS) {
                throw new BadRequest("Task status must be IN_PROGRESS to submit for confirmation.");
            }

            if (existingTask.getProviderId() == null || !existingTask.getProviderId().equals(providerId)) {
                throw new BadRequest("You are not authorized to complete this task.");
            }

            Task updatedTask = taskMapper.updateStatusToPendingConfirmation(id, providerId);

            if (updatedTask == null) {
                throw new BadRequest("Task completion update failed. The task state may have changed.");
            }

            return updatedTask;

        } catch (NotFound | BadRequest e) {
            log.error("Task Pending Confirmation Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task completion for task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while updating task completion");
        }
    }

    /**
     * Approves task completion by the client.
     *
     * @param id the unique identifier (ID) of the task to approve
     * @param email the email address of the client approving the task completion
     * @return the updated Task object with status changed to "COMPLETED"
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task status is not PENDING_CONFIRMATION or if the user is not the task's client (not authorized to approve)
     * @throws InternalServerError if an unexpected error occurs during approval
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task approveTaskCompletion(String id, String email) {
        try {
            Task existingTask = getTaskById(id);

            User user = userMapper.getUserByEmail(email);
            if (user == null) {
                throw new NotFound("User with email " + email + " not found");
            }
            String clientId = user.getId();

            if (existingTask.getStatus() != TaskStatus.PENDING_CONFIRMATION) {
                throw new BadRequest("Task status must be PENDING CONFIRMATION to submit for completion.");
            }

            if (!existingTask.getClientId().equals(clientId)) {
                throw new BadRequest("You are not authorized to complete this task.");
            }

            Task updatedTask = taskMapper.updateStatusToCompleted(id, clientId);

            if (updatedTask == null) {
                throw new BadRequest("Task completion update failed. The task state may have changed.");
            }

            return updatedTask;

        } catch (NotFound | BadRequest e) {
            log.error("Approve Task Completion Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task completion for task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while updating task completion");
        }
    }


    @Override
    public Task cancelTask(String id, String email) {
        return null;
    }



    private static String getProviderId(User user, Task existingTask) {
        String providerId = user.getId();

        // Check if existing task status != 'PENDING' and providerId IS NOT NULL
        if (
                existingTask.getStatus() != TaskStatus.PENDING || existingTask.getProviderId() != null
        ) {
            throw new BadRequest("Task is not available to be accepted");
        }

        // Check if PROVIDER wants to accept their own task
        if (existingTask.getClientId().equals(providerId)) {
            throw new BadRequest("You cannot accept your own task");
        }
        return providerId;
    }


}
