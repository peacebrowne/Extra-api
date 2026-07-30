package com.example.extra.Services.Impl;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.Task;
import com.example.extra.Entities.User;
import com.example.extra.Enumerator.BudgetType;
import com.example.extra.Enumerator.Roles;
import com.example.extra.Enumerator.TaskStatus;
import com.example.extra.Enumerator.UrgencyType;
import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.NotFound;
import com.example.extra.Mappers.ImageMapper;
import com.example.extra.Mappers.TaskMapper;
import com.example.extra.Mappers.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    TaskMapper taskMapper;

    @Mock
    UserMapper userMapper;

    @Mock
    ImageMapper imageMapper;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    TaskServiceImpl taskServiceImpl;

    private Task task;
    private User clientUser;
    private User providerUser;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId("568155ac-9d47-40ec-b102-bcbfac87b22");
        clientUser.setEmail("emmanuelbrowne751999@gmail.com");
        clientUser.setFullName("Emmanuel Browne");
        clientUser.setRole(Roles.CLIENT);

        providerUser = new User();
        providerUser.setId("69cbd000-9a38-4146-86c4-deb14f37f8e4");
        providerUser.setEmail("echoes857@gmail.com");
        providerUser.setFullName("Echoes Browne");
        providerUser.setRole(Roles.PROVIDER);

        this.task = new Task();
        task.setSubCategoryId("3f887a64-181e-44dc-8f03-b58cb7f3f159");
        task.setTitle("Fix leaking kitchen sink pipe");
        task.setDescription("The pipe under the main kitchen sink has a noticeable crack and is leaking water whenever the faucet runs. Need someone to replace the section of the PVC pipe.");
        task.setClientId(clientUser.getId());
        task.setStatus(TaskStatus.PENDING);
        task.setBudgetType(BudgetType.FIXED);
        task.setBudgetAmount(130.00);
        task.setUrgencyType(UrgencyType.WITHIN_48_HOURS);
        task.setImages(List.of("image1.png", "image2.png"));
        task.setProviderId(null);
    }

    @Nested
    @DisplayName("Get Task By Id Test")
    class GetTaskByIdTest {

        @Test
        @DisplayName("Should return task successfully when valid id exist")
        void shouldReturnTaskSuccessfully() {
            final String taskId = task.getId();

            when(taskMapper.getTaskById(taskId)).thenReturn(task);

            final Task returnedTask = taskServiceImpl.getTaskById(taskId);

            assertNotNull(returnedTask);
            assertEquals(task.getId(), returnedTask.getId());
            verify(taskMapper, times(1)).getTaskById(taskId);
        }

        @Test
        @DisplayName("Should throw NotFound exception when task does not exist")
        void shouldThrowNotFoundExceptionWhenTaskDoesNotExist() {
            final String taskId = task.getId();

            when(taskMapper.getTaskById(taskId)).thenReturn(null);
            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.getTaskById(taskId));

            assertEquals("Task with id " + taskId + " not found", exception.getMessage());
            verify(taskMapper, times(1)).getTaskById(taskId);
        }
    }

    @Nested
    @DisplayName("Create Task Test")
    class CreateTaskTest {

        @Test
        @DisplayName("Should create task successfully when valid data exist")
        void shouldCreateTaskSuccessfully() {
            final String userId = "568155ac-9d47-40ec-b102-bcbfac87b223";

            UserDTO existingUser = userMapper.getUserById(userId);
            log.info(existingUser.toString());


            // Arrange
            when(userMapper.getUserByIdentifier(userId)).thenReturn(clientUser);
            // Dynamic mutation workaround: allows service layer to alter variables like scheduled_at
            when(taskMapper.createTask(any(Task.class))).thenReturn(task);

            // Act
            final Task createdTask = taskServiceImpl.createTask(task, userId);

            // Assert
            assertNotNull(createdTask);
            assertEquals(task.getId(), createdTask.getId());

            // Verify
            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verify(taskMapper, times(1)).createTask(argThat(t -> t.getId().equals(task.getId())));
        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
            final String userId = task.getClientId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(null);
            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.createTask(task, userId));

            assertEquals("User with this identifier " + userId + " not found", exception.getMessage());

            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    @DisplayName("Update Task Test")
    class UpdateTaskTest {

        Task expectedTask;

        @BeforeEach
        void setUp() {
            expectedTask = new Task();
            expectedTask.setId(task.getId());
            expectedTask.setClientId(task.getClientId());
            expectedTask.setTitle("Updated Test Task Title");
            expectedTask.setDescription("Updated Test Task Description");
        }

        @Test
        @DisplayName("Should update task successfully when valid data exist")
        void shouldUpdateTaskSuccessfully() {
            final String userId = expectedTask.getClientId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(clientUser);
            when(taskMapper.getTaskById(task.getId())).thenReturn(task);

            final Task updatedTask = taskServiceImpl.updateTask(task.getId(), expectedTask, userId);

            assertNotNull(updatedTask);
            assertEquals(expectedTask.getTitle(), updatedTask.getTitle());
            assertEquals(expectedTask.getDescription(), updatedTask.getDescription());
            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verify(taskMapper, times(1)).updateTask(argThat(t -> t.getId().equals(expectedTask.getId())));
        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
            final String userId = clientUser.getId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(null);
            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.updateTask(task.getId(),task, userId));

            assertEquals("User with this identifier " + userId + " not found", exception.getMessage());

            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verifyNoInteractions(taskMapper);
        }

        @Test
        @DisplayName("Should throw NotFound exception when task does not exist")
        void shouldThrowNotFoundExceptionWhenTaskDoesNotExist() {
            final String taskId = expectedTask.getId();

            when(userMapper.getUserByIdentifier(expectedTask.getClientId())).thenReturn(clientUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(null);

            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.updateTask(taskId, expectedTask, clientUser.getId()));
            assertEquals("Task with id " + taskId + " not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete Task Test")
    class DeleteTaskTest {

        @Test
        @DisplayName("Should delete task successfully with valid id")
        void shouldDeleteTaskSuccessfully() {
            String userEmail = clientUser.getEmail();

            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(clientUser);
            when(taskMapper.getTaskById(task.getId())).thenReturn(task);
            doNothing().when(taskMapper).deleteTask(task.getId());

            final Task deletedTask = taskServiceImpl.deleteTask(task.getId(), userEmail);
            assertEquals(task, deletedTask);
            verify(userMapper, times(1)).getUserByIdentifier(userEmail);
        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
            String userEmail = clientUser.getEmail();

            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(null);
            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.deleteTask(task.getId(), userEmail));

            assertEquals("User with this identifier " + userEmail + " not found", exception.getMessage());

            verify(userMapper, times(1)).getUserByIdentifier(userEmail);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    @DisplayName("Accept Task Test")
    class AcceptTaskTest {

        private Task acceptedTask;
        private String taskId;
        private String userEmail;

        @BeforeEach
        void setUp() {
            taskId = task.getId();
            userEmail = providerUser.getEmail();

            acceptedTask = new Task();
            acceptedTask.setId(task.getId());
            acceptedTask.setClientId(clientUser.getId());
            acceptedTask.setStatus(TaskStatus.ACCEPTED);
            acceptedTask.setProviderId(providerUser.getId());
        }

        @Test
        @DisplayName("Should accept task successfully with valid id")
        void shouldAcceptTaskSuccessfully() {
            when(taskMapper.getTaskById(task.getId()))
                    .thenReturn(task)
                    .thenReturn(acceptedTask);
            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(providerUser);

            doNothing().when(taskMapper).updateStatusToAccepted(taskId, providerUser.getId());

            Task resultTask = taskServiceImpl.acceptTask(taskId, userEmail);

            assertNotNull(resultTask);
            assertEquals(TaskStatus.ACCEPTED, resultTask.getStatus(), "The returned task status must be ACCEPTED");
            assertEquals(providerUser.getId(), resultTask.getProviderId());
            assertEquals(acceptedTask.getId(), resultTask.getId());

            verify(taskMapper, times(1)).updateStatusToAccepted(taskId, providerUser.getId());
            verify(taskMapper, times(2)).getTaskById(taskId);
        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(null);

            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.acceptTask(taskId, userEmail));
            assertEquals("User with this identifier " + userEmail + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NotFound exception when task does not exist")
        void shouldThrowNotFoundExceptionWhenTaskDoesNotExist() {
            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(providerUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(null);

            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.acceptTask(taskId, userEmail));
            assertEquals("Task with id " + taskId + " not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Start Task Test")
    class StartTaskTest {

        private Task startedTask;
        private String taskId;
        private String userEmail;

        @BeforeEach
        void setUp() {
            taskId = task.getId();
            userEmail = providerUser.getEmail();

            task.setStatus(TaskStatus.ACCEPTED);
            task.setProviderId(providerUser.getId());

            startedTask = new Task();
            startedTask.setId(task.getId());
            startedTask.setClientId(clientUser.getId());
            startedTask.setStatus(TaskStatus.IN_PROGRESS);
            startedTask.setProviderId(providerUser.getId());
        }

        @Test
        @DisplayName("Should start task successfully with valid id")
        void shouldStartTaskSuccessfully() {
            when(taskMapper.getTaskById(task.getId()))
                    .thenReturn(task)
                    .thenReturn(startedTask);
            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(providerUser);

            doNothing().when(taskMapper).updateStatusToInProgress(taskId, providerUser.getId());

            Task resultTask = taskServiceImpl.startTask(taskId, userEmail);

            assertNotNull(resultTask);
            assertEquals(TaskStatus.IN_PROGRESS, resultTask.getStatus(), "The returned task status must be IN_PROGRESS");
            assertEquals(providerUser.getId(), resultTask.getProviderId());
            assertEquals(startedTask.getId(), resultTask.getId());

            verify(taskMapper, times(1)).updateStatusToInProgress(taskId, providerUser.getId());
            verify(taskMapper, times(2)).getTaskById(taskId);
        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(null);

            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.startTask(taskId, userEmail));
            assertEquals("User with this identifier " + userEmail + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NotFound exception when task does not exist")
        void shouldThrowNotFoundExceptionWhenTaskDoesNotExist() {
            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(providerUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(null);

            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.startTask(taskId, userEmail));
            assertEquals("Task with id " + taskId + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw BadRequest when task is not in ACCEPTED status")
        void shouldThrowBadRequestWhenTaskIsNotInAcceptedStatus() {
            task.setStatus(TaskStatus.PENDING);

            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(providerUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(task);

            final BadRequest exception = assertThrows(BadRequest.class, () -> taskServiceImpl.startTask(taskId, userEmail));
            assertEquals("Task cannot be started unless it is in ACCEPTED status.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw BadRequest when user is not authorized to start task")
        void shouldThrowBadRequestWhenUserIsNotAuthorizedToStartTask() {
            task.setProviderId("different_user_id");

            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(providerUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(task);

            final BadRequest exception = assertThrows(BadRequest.class, () -> taskServiceImpl.startTask(taskId, userEmail));
            assertEquals("You are not authorized to start this task.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cancel Task Test")
    class CancelTaskTest {
        private String taskId;
        private String userEmail;

        @BeforeEach
        void setUp() {
            taskId = task.getId();
        }

        @ParameterizedTest
        @EnumSource(value = TaskStatus.class, names = { "PENDING", "ACCEPTED" })
        @DisplayName("Client should cancel task successfully with valid id when task is still pending")
        void ClientShouldCancelTaskSuccessfullyWhenTaskIsStillPending(TaskStatus status) {
            userEmail = clientUser.getEmail();
            task.setStatus(status);

            Task canceledTask = new Task();
            canceledTask.setId(taskId);
            canceledTask.setClientId(clientUser.getId());
            canceledTask.setStatus(TaskStatus.CANCELLED);

            given(userMapper.getUserByEmail(userEmail)).willReturn(clientUser);
            given(taskMapper.getTaskById(taskId)).willReturn(task).willReturn(canceledTask);

            doNothing().when(taskMapper).updateStatusToCancelled(taskId, clientUser.getId());

            Task resultTask = taskServiceImpl.cancelTask(taskId, userEmail);

            assertNotNull(resultTask);
            assertEquals(TaskStatus.CANCELLED, resultTask.getStatus(), "The returned task status must be CANCELLED");
            assertEquals(clientUser.getId(), resultTask.getClientId());

            then(taskMapper).should(times(1)).updateStatusToCancelled(taskId, clientUser.getId());
            then(taskMapper).should(times(2)).getTaskById(taskId);
        }

        @Test
        @DisplayName("Provider should revert task to PENDING when task has been accepted but not started")
        void ProviderShouldCancelTaskSuccessfullyWhenTaskHasBeenAccepted() {
            userEmail = providerUser.getEmail();
            task.setProviderId(providerUser.getId());
            task.setStatus(TaskStatus.ACCEPTED);

            Task pendingTask = new Task();
            pendingTask.setId(taskId);
            pendingTask.setClientId(clientUser.getId());
            pendingTask.setProviderId(providerUser.getId());
            pendingTask.setStatus(TaskStatus.PENDING);

            when(userMapper.getUserByEmail(userEmail)).thenReturn(providerUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(task).thenReturn(pendingTask);

            doNothing().when(taskMapper).updateStatusToPending(taskId, providerUser.getId());

            Task resultTask = taskServiceImpl.cancelTask(taskId, userEmail);

            assertNotNull(resultTask);
            assertEquals(TaskStatus.PENDING, resultTask.getStatus(), "The returned task status must be PENDING");
            assertEquals(providerUser.getId(), resultTask.getProviderId());

            verify(taskMapper, times(1)).updateStatusToPending(taskId, providerUser.getId());
            verify(taskMapper, times(2)).getTaskById(taskId);
        }

        @ParameterizedTest
        @EnumSource(value = TaskStatus.class, names = { "COMPLETED", "CANCELLED", "DISPUTED" })
        @DisplayName("Should Throw BadRequest when Task status is COMPLETED, CANCELLED or DISPUTED")
        void shouldThrowBadRequestWhenTaskStatusIsCompletedOrCancelledOrDisputed(TaskStatus status) {
            userEmail = clientUser.getEmail();
            task.setStatus(status);

            when(userMapper.getUserByEmail(userEmail)).thenReturn(clientUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(task);

            final BadRequest exception = assertThrows(BadRequest.class, () -> taskServiceImpl.cancelTask(taskId, userEmail));

            assertEquals("Task is already finalized or disputed and cannot be canceled.", exception.getMessage());
            then(taskMapper).shouldHaveNoMoreInteractions();
        }

        @Disabled("Temporary disabled")
        @ParameterizedTest
        @ValueSource(strings = {"ADMIN", "GUEST", "SUPPORT"})
        @DisplayName("Should throw BadRequest when user role for cancellation is invalid")
        void shouldThrowBadRequestWhenUserRoleIsInvalid(String role) {
            userEmail = clientUser.getEmail();
            clientUser.setRole(Roles.valueOf(role));

            given(userMapper.getUserByEmail(userEmail)).willReturn(clientUser);
            given(taskMapper.getTaskById(taskId)).willReturn(task);

            assertThatThrownBy(() -> taskServiceImpl.cancelTask(taskId, userEmail))
                    .isInstanceOf(BadRequest.class)
                    .hasMessage("Invalid user role for cancellation");

            then(userMapper).shouldHaveNoMoreInteractions();
            then(taskMapper).shouldHaveNoMoreInteractions();
        }
    }
}