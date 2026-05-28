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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

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

            log.info("\n\n Retrieving task from db \n\n");

            final Task task = taskMapper.getTaskById(id);
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
     * Creates a new task and saves it to the database.
     *
     * @param task the Task object to create (should include title, description, clientId, etc.)
     * @return the created Task object with an assigned id
     * @throws NotFound if no task with the given id exists
     * @throws InternalServerError if an unexpected error occurs during task creation
     */
    @Override
    @CachePut(value = "task", key = "#result.id")
    public Task createTask(Task task) {
        try {
            // Check if the CLIENT exists before creating the task
            checkAndReturnUser(task.getClientId());

            // Create and save the task to the database
            return taskMapper.createTask(task);

        } catch (NotFound ex){
            log.error(ex.getMessage());
            throw ex;
        } catch (Exception e) {
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
    @CachePut(value = "task", key = "#task.id")
    public Task updateTask(Task task) {
        try {
            checkAndReturnUser(task.getClientId());

            // Retrieve and verify the task exists
            final Task existingTask = getTaskById(task.getId());

            // Update title and description only if provided (non-null)
            Optional.ofNullable(task.getTitle()).ifPresent(existingTask::setTitle);
            Optional.ofNullable(task.getDescription()).ifPresent(existingTask::setDescription);

            // Save the updated task to the database
            taskMapper.updateTask(task);

            return getTaskById(task.getId());

        } catch (NotFound ex){
            log.error(ex.getMessage());
            throw ex;
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
    public Task deleteTask(String id, String email) {
        try {
            checkAndReturnUser(email);

            // Verify the task exists before attempting deletion
            final Task existingTask = getTaskById(id);
            taskMapper.deleteTask(id);

            return existingTask;
        } catch (BadRequest | NotFound e) {
            log.error("ERROR: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while deleting task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while deleting task");
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
            final User user = checkAndReturnUser(email);
            final Task existingTask = getTaskById(id);

            // Check if existing task status != 'PENDING' and providerId IS NOT NULL
            if (existingTask.getStatus() != TaskStatus.PENDING ||
                    existingTask.getProviderId() != null) {
                throw new BadRequest("Task is not available to be accepted");
            }

            // Check if PROVIDER wants to accept their own task
            if (existingTask.getClientId().equals(user.getId())) {
                throw new BadRequest("You cannot accept your own task");
            }

            taskMapper.updateStatusToAccepted(id, user.getId());

            return getTaskById(id);

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
     * provider (not authorized to start this task)
     * @throws InternalServerError if an unexpected error occurs during the operation
     *
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task startTask(String id, String email) {
        try {
            // Retrieve and verify the provider exists
            final User user = checkAndReturnUser(email);

            // Retrieve and verify the task exists
            final Task existingTask = getTaskById(id);

            // Validate task status is ACCEPTED
            if (existingTask.getStatus() != TaskStatus.ACCEPTED) {
                throw new BadRequest("Task cannot be started unless it is in ACCEPTED status.");
            }

            if (existingTask.getProviderId() == null || !existingTask.getProviderId().equals(user.getId())) {
                throw new BadRequest("You are not authorized to start this task.");
            }

            // Update task status to IN_PROGRESS
            taskMapper.updateStatusToInProgress(id, user.getId());

            return getTaskById(id);

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
     * provider (not authorized to submit for confirmation)
     * @throws InternalServerError if an unexpected error occurs during the operation
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task taskPendingConfirmation(String id, String email) {
        try {
            final Task existingTask = getTaskById(id);

            final User user = checkAndReturnUser(email);
            final String providerId = user.getId();

            if (existingTask.getStatus() != TaskStatus.IN_PROGRESS) {
                throw new BadRequest("Task status must be IN_PROGRESS to submit for confirmation.");
            }

            if (existingTask.getProviderId() == null || !existingTask.getProviderId().equals(providerId)) {
                throw new BadRequest("You are not authorized to complete this task.");
            }

            taskMapper.updateStatusToPendingConfirmation(id, providerId);

            return getTaskById(id);

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
            final Task existingTask = getTaskById(id);

            final User user = checkAndReturnUser(email);
            final String clientId = user.getId();

            if (existingTask.getStatus() != TaskStatus.PENDING_CONFIRMATION) {
                throw new BadRequest("Task status must be PENDING CONFIRMATION to submit for completion.");
            }

            if (!existingTask.getClientId().equals(clientId)) {
                throw new BadRequest("You are not authorized to complete this task.");
            }

            taskMapper.updateStatusToCompleted(id, clientId);

            return getTaskById(id);

        } catch (NotFound | BadRequest e) {
            log.error("Approve Task Completion Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task completion for task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while updating task completion");
        }
    }

    /**
     * Cancels a task with context-aware behavior based on the requester's role.
     *
     * @param id the unique identifier (ID) of the task to cancel
     * @param email the email address of the user requesting cancellation
     * @return the updated Task object with status changed to CANCELLED, PENDING, or DISPUTED
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task is already finalized (COMPLETED, CANCELLED, or DISPUTED)
     *                    or if the user is not authorized to cancel (not the client or assigned provider)
     * @throws InternalServerError if an unexpected error occurs during cancellation
     *
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task cancelTask(String id, String email) {
        try {
            // Retrieve and verify the user exists
            final User user = checkAndReturnUser(email);

            // Retrieve and verify the task exists
            final Task existingTask = getTaskById(id);

            // Validate task is not already finalized
            if (
                    existingTask.getStatus().equals(TaskStatus.COMPLETED) ||
                    existingTask.getStatus().equals(TaskStatus.CANCELLED) ||
                    existingTask.getStatus().equals(TaskStatus.DISPUTED)) {
                throw new BadRequest("Task is already finalized or disputed and cannot be canceled.");
            }

            // Delegate to role-specific cancellation logic
            clientInitiateCancel(id, user, existingTask);
            providerInitiateCancel(id, user, existingTask);

            return getTaskById(id);

        }catch (NotFound | BadRequest e) {
            log.error("Can't find task id {} for email {}", id, email);
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error occurred while canceling task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while canceling the task");
        }
    }


    /**
     * Handles task cancellation when initiated by a client.
     *
     * @param id the unique identifier (ID) of the task to cancel
     * @param user the User object of the client initiating cancellation
     * @param task the Task object being cancelled
     * @throws BadRequest if the user is not the task's client (not authorized)
     *
     * Note: TODO - integrate provider partial payment handling before marking as DISPUTED.
     */
    private void clientInitiateCancel(String id, User user, Task task) {

        final String role = user.getRole().name();
        final String userId = user.getId();

        // When a Client Initiates Cancellation
        if(role.equals("CLIENT")) {

            // Verify the user is the task's client
            if (!task.getClientId().equals(userId)) {
                throw new BadRequest("You are not authorized to cancel this task as a client.");
            }

            log.info("Client Initiate Cancel Task");

            // Cancel if task not yet started (PENDING or ACCEPTED)
            if(task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.ACCEPTED) {
                log.info("Client Pending Cancel Task");
                taskMapper.updateStatusToCancelled(id, userId);
            }
            // Dispute if work is already in progress
            else if(task.getStatus() == TaskStatus.IN_PROGRESS) {

                // TODO - Handle provider partial payment calculation before marking dispute

                log.info("Client In Progress Cancel Task");
                taskMapper.updateStatusToDisputed(id, userId);
            }

        }

    }

    /**
     * Handles task cancellation when initiated by a provider.
     *
     * @param id the unique identifier (ID) of the task to cancel
     * @param user the User object of the provider initiating cancellation
     * @param task the Task object being cancelled
     * @throws BadRequest if the user is not the assigned provider (not authorized)
     *
     * Note: TODO - integrate task re-marketing logic to attract new providers when status reverts to PENDING.
     */
    private void providerInitiateCancel(String id, User user, Task task) {

        final String role = user.getRole().name();
        final String userId = user.getId();

        // When a Provider Initiates Cancellation
        if(role.equals("PROVIDER")) {

            // Verify the user is the assigned provider
            if (task.getProviderId() == null || !task.getProviderId().equals(userId)){
                throw new BadRequest("You are not authorized to cancel this task as a provider.");
            }

            log.info("Provider Initiate Cancel Task");

            // Revert to PENDING if provider cancels before starting work
            if(task.getStatus() == TaskStatus.ACCEPTED) {
                log.info("Provider Accepted Cancel Task");

                // TODO - Re-market the task to attract new providers (notification, listing update, etc.)

                taskMapper.updateStatusToPending(id, userId);
            }

            // Dispute if provider cancels during active work
            if(task.getStatus() == TaskStatus.IN_PROGRESS) {
                log.info("Provider In Progress Cancel Task");
                taskMapper.updateStatusToDisputed(id, userId);
            }
        }
    }

    private User checkAndReturnUser(String identifier){
        final User user = userMapper.getUserByEmail(identifier);
        if (user == null) {
            throw new NotFound("User with this identifier " + identifier + " not found");
        }
        return user;
    }

    private static String getProviderId(User user, Task existingTask) {
        final String providerId = user.getId();

        // Check if PROVIDER wants to accept their own task
        if (existingTask.getClientId().equals(providerId)) {
            throw new BadRequest("You cannot accept your own task");
        }
        return providerId;
    }


}
