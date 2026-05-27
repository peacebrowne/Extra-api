package com.example.extra.Services.Impl;

import com.example.extra.Entities.Task;
import com.example.extra.Entities.User;
import com.example.extra.Enumerator.Roles;
import com.example.extra.Enumerator.TaskStatus;
import com.example.extra.Exceptions.Custom.NotFound;
import com.example.extra.Mappers.TaskMapper;
import com.example.extra.Mappers.UserMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    /*
     * Inject Dependencies that your Service Implementation depends on
     */
    @Mock
    TaskMapper taskMapper;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    TaskServiceImpl taskServiceImpl;

    private Task task;
    private User clientUser;
    private User providerUser;


    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId("123e4567-e89b-12d3-a456-426614174001");
        clientUser.setEmail("emmanuelbrowne751999@gmail.com");
        clientUser.setFirstName("Emmanuel");
        clientUser.setLastName("Browne");
        clientUser.setRole(Roles.CLIENT);

        providerUser = new User();
        providerUser.setId("69cbd000-9a38-4146-86c4-deb14f37f8e4");
        providerUser.setEmail("echoes857@gmail.com");
        providerUser.setFirstName("Echoes");
        providerUser.setLastName("Browne");
        providerUser.setRole(Roles.PROVIDER);

        this.task = new Task();
        task.setId("6764842f-770a-434f-b3de-1ee9bbaa0b61");
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setClientId(clientUser.getId());
        task.setStatus(TaskStatus.PENDING);
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
    class CreateTaskTest{

        @Test
        @DisplayName("Should create task successfully when valid data exist")
        void shouldCreateTaskSuccessfully(){
            final String userId = task.getClientId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(clientUser);
            when(taskMapper.createTask(task)).thenReturn(task);

            final Task createdTask = taskServiceImpl.createTask(task);

            assertNotNull(createdTask);
            assertEquals(task.getId(), createdTask.getId());
            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verify(taskMapper, times(1)).createTask(task);
            verify(taskMapper, times(1)).createTask(argThat(t -> t.getId().equals(createdTask.getId())));

        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist(){

            final String userId = task.getClientId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(null);
            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.createTask(task));

            assertEquals("User with this identifier " + userId + " not found", exception.getMessage());

            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verifyNoInteractions(taskMapper);
        }


    }

    @Nested
    @DisplayName("Update Task Test")
    class UpdateTaskTest{

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
        void shouldUpdateTaskSuccessfully(){
            // Arrange
            final String userId = expectedTask.getClientId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(clientUser);
            when(taskMapper.getTaskById(task.getId())).thenReturn(task);

            // Act
            final Task updatedTask = taskServiceImpl.updateTask(expectedTask);

            // Assert || Verify
            assertNotNull(updatedTask);
            assertEquals(expectedTask.getTitle(), updatedTask.getTitle());
            assertEquals(expectedTask.getDescription(), updatedTask.getDescription());
            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verify(taskMapper, times(1)).updateTask(argThat(t -> t.getId().equals(expectedTask.getId())));
        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist(){

            final String userId = clientUser.getId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(null);
            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.updateTask(task));

            assertEquals("User with this identifier " + userId + " not found", exception.getMessage());

            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verifyNoInteractions(taskMapper);
        }

        @Test
        @DisplayName("Should throw NotFound exception when task does not exist")
        void shouldThrowNotFoundExceptionWhenTaskDoesNotExist(){
            final String taskId = expectedTask.getId();

            when(userMapper.getUserByIdentifier(expectedTask.getClientId())).thenReturn(clientUser);
            when(taskMapper.getTaskById(taskId)).thenReturn(null);

            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.updateTask(expectedTask));
            assertEquals("Task with id " + taskId + " not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete Task Test")
    class DeleteTaskTest{

        @Test
        @DisplayName("Should delete task successfully with valid id")
        void shouldDeleteTaskSuccessfully(){
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
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist(){

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

        private Task completedTask;
        private String taskId;
        private String userEmail;

        @BeforeEach
        void setUp() {
            taskId = task.getId();
            userEmail = providerUser.getEmail();

            completedTask = new Task();
            completedTask.setId(task.getId());
            completedTask.setClientId(clientUser.getId());
            completedTask.setStatus(TaskStatus.ACCEPTED);
            completedTask.setProviderId(providerUser.getId());
        }

        @Test
        @DisplayName("Should accept task successfully with valid id")
        void shouldAcceptTaskSuccessfully() {
            when(taskMapper.getTaskById(task.getId()))
                    .thenReturn(task)
                    .thenReturn(completedTask);
            when(userMapper.getUserByIdentifier(userEmail)).thenReturn(providerUser);

            doNothing().when(taskMapper).updateStatusToAccepted(taskId, providerUser.getId());

            Task resultTask = taskServiceImpl.acceptTask(taskId, userEmail);

            assertNotNull(resultTask);
            assertEquals(TaskStatus.ACCEPTED, resultTask.getStatus(), "The returned task status must be ACCEPTED");
            assertEquals(providerUser.getId(), resultTask.getProviderId());

            assertEquals(completedTask.getId(), resultTask.getId(), "");

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



}


















