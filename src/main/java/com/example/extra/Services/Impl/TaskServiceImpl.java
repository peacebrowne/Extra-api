package com.example.extra.Services.Impl;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.Location;
import com.example.extra.Entities.Task;
import com.example.extra.Entities.User;
import com.example.extra.Enumerator.TaskStatus;
import com.example.extra.Enumerator.UrgencyType;
import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Exceptions.Custom.NotFound;
import com.example.extra.Mappers.TaskMapper;
import com.example.extra.Services.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ImageServiceImpl imageServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private final LocationServiceImpl locationServiceImpl;

    /**
     * Retrieves all tasks associated with a given user identifier.
     *
     * @param identifier the email address of the user whose tasks are to be retrieved
     * @return a list of all tasks for the specified user; returns an empty list if no tasks exist
     * @throws InternalServerError if an unexpected error occurs during task retrieval
     */
    @Override
    @Cacheable(value = "tasks", key = "#identifier")
    public List<Task> getAllTasks(String identifier) {
        try {
            User user = userServiceImpl.getUserByEmail(identifier);
            List<Task> tasks = taskMapper.getAllTasks(user.getId());
            tasks.forEach(task -> {

                task.setLocation(addTaskLocation(task.getId()));

                // Add task provider if existing
                task.setProvider(addTaskProvider(task.getProviderId()));

                // Add task images to task
                task.setImages(addTaskImages(task.getId()));

            });
            return tasks;
        } catch (Exception e) {
            log.error("Unexpected error occurred while retrieving all tasks: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while retrieving all tasks", e);
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

            final Task task = taskMapper.getTaskById(id);
            if (task == null) {
                log.warn("Task with id {} not found", id);
                throw new NotFound("Task with id " + id + " not found");
            }

            // Add task location to task
            Location location = locationServiceImpl.getLocation(task.getId());
            task.setLocation(location);

            // Add task images to task
            List<String> images = imageServiceImpl.getImages(task.getId());
            if (images != null && !images.isEmpty()) {
                task.setImages(images);
            }

            return task;
        } catch (NotFound e) {
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while retrieving task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while retrieving task", e);
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
    public Task createTask(Task task, String identifier) {
        try {

            validateTaskOwnership(task.getClientId(),  identifier);

            // Handle Scheduled Date Time
            handleScheduleDateTime(task);

            Task createdTask =  taskMapper.createTask(task);
            createdTask.setImages(task.getImages());

            // Store task location
            Location location = locationServiceImpl.createLocation(createdTask.getId() ,task.getLocation());
            createdTask.setLocation(location);

            // Store task images if provded
            imageServiceImpl.storeImages(createdTask.getId(), task.getImages());

            // Notify available PROVIDERS that a new task is created for bidding
            messagingTemplate.convertAndSend("/topic/provider-tasks", createdTask);

            // Create and save the task to the database
            return createdTask;

        } catch (NotFound | BadRequest e){
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while creating task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while creating task", e);
        }
    }

    /**
     * Updates an existing task with new values.
     *
     * @param id the unique identifier (ID) of the task to update
     * @param task the Task object containing updated field values (id is required)
     * @return the updated Task object
     * @throws NotFound if no task with the given id exists
     * @throws InternalServerError if an unexpected error occurs during update
     *
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task updateTask(String id, Task task, String identifier) {
        try {

            // Retrieve and verify the task exists
            final Task existingTask = getTaskById(id);

            validateTaskOwnership(existingTask.getClientId(), identifier);

            // Update string fields if provided
            Optional.ofNullable(task.getTitle()).ifPresent(existingTask::setTitle);
            Optional.ofNullable(task.getDescription()).ifPresent(existingTask::setDescription);

            // Update enum fields if provided
            Optional.ofNullable(task.getBudgetType()).ifPresent(existingTask::setBudgetType);
            Optional.ofNullable(task.getUrgencyType()).ifPresent(existingTask::setUrgencyType);
            Optional.ofNullable(task.getPropertyType()).ifPresent(existingTask::setPropertyType);

            // Update other fields if provided
            Optional.ofNullable(task.getBudgetAmount()).ifPresent(existingTask::setBudgetAmount);
            Optional.ofNullable(task.getSubCategoryId()).ifPresent(existingTask::setSubCategoryId);
            Optional.ofNullable(task.getImages()).ifPresent(existingTask::setImages);
            imageServiceImpl.updateImages(existingTask.getId(), task.getImages());
            handleScheduleDateTime(existingTask);

            // Save the updated task to the database
            return taskMapper.updateTask(existingTask);

        } catch (NotFound e){
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while updating task", e);
        }
    }

    private void handleScheduleDateTime (Task task) {
        UrgencyType urgencyType = task.getUrgencyType();

        log.info("Urgency Type: {}", urgencyType);


        if (urgencyType == null) return;

        if (urgencyType == UrgencyType.FLEXIBLE && task.getScheduledAt() == null) {
            throw new BadRequest("Scheduled date and time is required when urgency type is FLEXIBLE");
        }

        switch (urgencyType) {
            case EMERGENCY:
                task.setScheduledAt(LocalDateTime.now());
                log.info("EMERGENCY Schedule Date: {}", task.getScheduledAt());
                break;
            case WITHIN_48_HOURS:
                task.setScheduledAt(LocalDateTime.now().plusDays(2));
                log.info("WITHIN_48_HOURS Schedule Date: {}", task.getScheduledAt());
                break;
            case THIS_WEEK:
                task.setScheduledAt(LocalDateTime.now().plusDays(7));
                log.info("THIS_WEEK Schedule Date: {}", task.getScheduledAt());

        }
    }

    private void validateTaskOwnership(String clientId, String identifier) {

        userServiceImpl.checkUserExistenceById(clientId);

        User user = userServiceImpl.getUserByEmail(identifier);

        if (!user.getId().equals(clientId)) {
            throw new BadRequest("Client ID does not match authenticated user");
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
    public Task deleteTask(String id, String identifier) {
        try {

            // Verify the task exists before attempting deletion
            final Task existingTask = getTaskById(id);
            validateTaskOwnership(existingTask.getClientId(), identifier);
            taskMapper.deleteTask(id);

            return existingTask;
        } catch (BadRequest | NotFound e) {
            log.error("ERROR: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while deleting task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while deleting task", e);
        }
    }

    /**
     * Marks a task as accepted by a provider.
     *
     * @param id the unique identifier (ID) of the task to accept
     * @param identifier the unique email of the current logged-in that's accepting the task
     * @return the updated Task object with status changed to "ACCEPTED"
     * @throws NotFound            if no task with the given id exists
     * @throws BadRequest          if the task cannot be accepted (e.g., already accepted or completed)
     * @throws InternalServerError if an unexpected error occurs during acceptance
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task acceptTask(String id, String identifier) {
        try {
            final User user = userServiceImpl.getUserByEmail(identifier);

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

            return taskMapper.updateStatusToAccepted(id, user.getId());


        } catch (NotFound | BadRequest e) {
            log.error("Accepting Task Validation Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while accepting task: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while accepting task", e);
        }
    }

    /**
     * Transitions a task from ACCEPTED to IN_PROGRESS when the provider begins work.
     *
     * @param id the unique identifier (ID) of the task to start
     * @param identifier the email address of the provider starting the task
     * @return the updated Task object with status changed to "IN_PROGRESS"
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task status is not ACCEPTED or if the user is not the assigned
     * provider (not authorized to start this task)
     * @throws InternalServerError if an unexpected error occurs during the operation
     *
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task startTask(String id, String identifier) {
        try {
            // Retrieve and verify the provider exists
            final User user = userServiceImpl.getUserByEmail(identifier);

            // Retrieve and verify the task exists
            final Task existingTask = getTaskById(id);

            // Validate task status is ACCEPTED
            if (existingTask.getStatus() != TaskStatus.ACCEPTED) {
                throw new BadRequest("Task cannot be started unless it is in ACCEPTED status.");
            }

            if (existingTask.getProviderId() == null ||
                    !existingTask.getProviderId().equals(user.getId())) {
                throw new BadRequest("You are not authorized to start this task.");
            }

            // Update task status to IN_PROGRESS
            return taskMapper.updateStatusToInProgress(id, user.getId());


        } catch (NotFound | BadRequest e) {
            log.error("Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error starting task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while starting the task", e);
        }
    }

    /**
     * Marks a task as pending confirmation after provider work completion
     * and await the client's confirmation.
     *
     * @param id the unique identifier (ID) of the task
     * @param identifier the email address of the provider submitting the task for confirmation
     * @return the updated Task object with status changed to "PENDING_CONFIRMATION"
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task status is not IN_PROGRESS or if the user is not the assigned
     * provider (not authorized to submit for confirmation)
     * @throws InternalServerError if an unexpected error occurs during the operation
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task taskPendingConfirmation(String id, String identifier) {
        try {
            final Task existingTask = getTaskById(id);

            final User user = userServiceImpl.getUserByEmail(identifier);
            final String providerId = user.getId();

            if (existingTask.getStatus() != TaskStatus.IN_PROGRESS) {
                throw new BadRequest("Task status must be IN_PROGRESS to submit for confirmation.");
            }

            if (existingTask.getProviderId() == null || !existingTask.getProviderId().equals(providerId)) {
                throw new BadRequest("You are not authorized to complete this task.");
            }

            return taskMapper.updateStatusToPendingConfirmation(id, providerId);


        } catch (NotFound | BadRequest e) {
            log.error("Task Pending Confirmation Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task completion for task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while updating task completion", e);
        }
    }

    /**
     * Approves task completion by the client.
     *
     * @param id the unique identifier (ID) of the task to approve
     * @param identifier the email address of the client approving the task completion
     * @return the updated Task object with status changed to "COMPLETED"
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task status is not PENDING_CONFIRMATION or if the user is not the task's client (not authorized to approve)
     * @throws InternalServerError if an unexpected error occurs during approval
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task approveTaskCompletion(String id, String identifier) {
        try {
            final Task existingTask = getTaskById(id);

            final User user = userServiceImpl.getUserByEmail(identifier);
            final String clientId = user.getId();

            if (existingTask.getStatus() != TaskStatus.PENDING_CONFIRMATION) {
                throw new BadRequest("Task status must be PENDING CONFIRMATION to submit for completion.");
            }

            if (!existingTask.getClientId().equals(clientId)) {
                throw new BadRequest("You are not authorized to complete this task.");
            }

            return taskMapper.updateStatusToCompleted(id, clientId);


        } catch (NotFound | BadRequest e) {
            log.error("Approve Task Completion Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task completion for task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while updating task completion", e);
        }
    }

    /**
     * Cancels a task with context-aware behavior based on the requester's role.
     *
     * @param id the unique identifier (ID) of the task to cancel
     * @param identifier the email address of the user requesting cancellation
     * @return the updated Task object with status changed to CANCELLED, PENDING, or DISPUTED
     * @throws NotFound if no task with the given id exists or the user is not found
     * @throws BadRequest if the task is already finalized (COMPLETED, CANCELLED, or DISPUTED)
     *                    or if the user is not authorized to cancel (not the client or assigned provider)
     * @throws InternalServerError if an unexpected error occurs during cancellation
     *
     */
    @Override
    @CachePut(value = "task", key = "#id")
    public Task cancelTask(String id, String identifier) {
        try {
            // Retrieve and verify the user exists
            final User user = userServiceImpl.getUserByEmail(identifier);

            // Retrieve and verify the task exists
            final Task existingTask = getTaskById(id);

            // Validate task is not already finalized
            if (
                    existingTask.getStatus().equals(TaskStatus.COMPLETED) ||
                    existingTask.getStatus().equals(TaskStatus.CANCELLED) ||
                    existingTask.getStatus().equals(TaskStatus.DISPUTED)) {
                throw new BadRequest("Task is already finalized or disputed and cannot be canceled.");
            }

            return switch (user.getRole()){
                // When a Client Initiates Cancellation
                case CLIENT -> clientInitiateCancel(id, user, existingTask);
                case PROVIDER -> providerInitiateCancel(id, user, existingTask);
            };


        }catch (NotFound | BadRequest e) {
            log.error("Can't find task id {} for email {}", id, identifier);
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error occurred while canceling task {}: {}", id, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while canceling the task", e);
        }
    }

    @Override
    public List<Task> searchUserTask(String identifier, String term, TaskStatus status) {
        try {
            User user  = userServiceImpl.getUserByEmail(identifier);
            String s = status != null ? status.name() : null;
            List<Task> tasks = taskMapper.getUserSearchTask(user.getId(), term, s);
            tasks.forEach(task -> {

                // Add task location to task
                task.setLocation(addTaskLocation(task.getId()));

                // Add task provider if existing
                task.setProvider(addTaskProvider(task.getProviderId()));

                // Add task images to task
               task.setImages(addTaskImages(task.getId()));

            });
            return tasks;
        } catch (Exception e) {
            log.error("Unexpected error occurred while searching user task {}: {}", identifier, e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while searching user tasks", e);
        }
    }

    private Location addTaskLocation(String taskId) {
        return locationServiceImpl.getLocation(taskId);
    }

    private UserDTO addTaskProvider(String providerId){
        return providerId != null ?  userServiceImpl.getUserById(providerId) : null;
    }

    private List<String> addTaskImages(String taskId){
        List<String> images = imageServiceImpl.getImages(taskId);
        return images != null && !images.isEmpty() ? images : new ArrayList<>();
    }

    /**
     * Handles task cancellation when initiated by a client.
     *
     * @param id the unique identifier (ID) of the task to cancel
     * @param user the User object of the client initiating cancellation
     * @param task the Task object being canceled
     * @throws BadRequest if the user is not the task's client (not authorized)
     *
     * Note: TODO - integrate provider partial payment handling before marking as DISPUTED.
     */
    private Task clientInitiateCancel(String id, User user, Task task) {

        final String userId = user.getId();


            // Verify the user is the task's client
            if (!task.getClientId().equals(userId)) throw new BadRequest("You are not authorized to cancel this task as a client.");

            log.info("Client Initiate Cancel Task");

            // Cancel if task not yet started (PENDING or ACCEPTED)
            if(task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.ACCEPTED) {
                log.info("Client Pending Cancel Task");
                return taskMapper.updateStatusToCancelled(id, userId);
            }
            // Dispute if work is already in progress
            else if(task.getStatus() == TaskStatus.IN_PROGRESS) {

                // TODO - Handle provider partial payment calculation before marking dispute

                log.info("Client Cancel Task In Progress");
                return taskMapper.updateStatusToDisputed(id, userId);

            }

            return task;

    }

    /**
     * Handles task cancellation when initiated by a provider.
     *
     * @param id the unique identifier (ID) of the task to cancel
     * @param user the User object of the provider initiating cancellation
     * @param task the Task object being canceled
     * @throws BadRequest if the user is not the assigned provider (not authorized)
     *
     * Note: TODO - integrate task re-marketing logic to attract new providers when status reverts to PENDING.
     */
    private Task providerInitiateCancel(String id, User user, Task task) {

        final String userId = user.getId();

        // When a Provider Initiates Cancellation

            // Verify the user is the assigned provider
            if (task.getProviderId() == null || !task.getProviderId().equals(userId) || task.getStatus() == TaskStatus.PENDING)
                throw new BadRequest("You are not authorized to cancel this task as a provider.");

            log.info("Provider Initiate Cancel Task");

            // Revert to PENDING if provider cancels before starting work
            if(task.getStatus() == TaskStatus.ACCEPTED) {
                log.info("Provider Accepted Cancel Task");

                // TODO - Re-market the task to attract new providers (notification etc.)

                return taskMapper.updateStatusToPending(id, userId);
            }

            // Dispute if provider cancels during active work
            if(task.getStatus() == TaskStatus.IN_PROGRESS) {
                log.info("Provider Cancel Task In Progress");

                return taskMapper.updateStatusToDisputed(id, userId);

            }

            return task;
    }

    /**
     * Retrieves all In Progress task for a specific user
     *
     * @param identifier the email address of the user requesting for In Progress Task
     * @return a list of In Progress tasks for the specified user; returns an empty list if no tasks exist
     * @throws BadRequest if the user is not the assigned provider (not authorized)
     * @throws NotFound if the user is not found
     */
    @Override
    @Cacheable(value = "tasks:in_progress", key = "#identifier")
    public List<Task> getInProgressTasks(String identifier) {
        try {
            final User user = userServiceImpl.getUserByEmail(identifier);
            final String userId = user.getId();
            return taskMapper.getInProgressTasks(userId);
        }catch (NotFound | BadRequest e) {
            log.error("Getting In Progress Tasks Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating task completion for task {}", e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while updating task completion", e);
        }
    }

}
