package com.example.extra.Services.Impl;

import com.example.extra.Entities.Task;
import com.example.extra.Entities.User;
import com.example.extra.Enumerator.Roles;
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
    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId("123e4567-e89b-12d3-a456-426614174001");
        user.setEmail("emmanuelbrowne751999@gmail.com");
        user.setFirstName("Emmanuel");
        user.setLastName("Browne");
        user.setRole(Roles.CLIENT);

        this.task = new Task();
        task.setId("6764842f-770a-434f-b3de-1ee9bbaa0b61");
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setClientId("123e4567-e89b-12d3-a456-426614174001");
    }


    @Nested
    @DisplayName("Create Task Test")
    class CreateTaskTest{

        @Test
        @DisplayName("Should create task successfully when valid data exist")
        void shouldCreateTaskSuccessfully(){
            final String userId = task.getClientId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(user);
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

        @Test
        @DisplayName("Should update task successfully when valid data exist")
        void shouldUpdateTaskSuccessfully(){
            final String userId = task.getClientId();

            task.setTitle("Updated Test Task Title");
            task.setDescription("Updated Test Task Description");

            when(userMapper.getUserByIdentifier(userId)).thenReturn(user);
            when(taskMapper.getTaskById(task.getId())).thenReturn(task);

            final Task updatedTask = taskServiceImpl.updateTask(task);

            assertNotNull(updatedTask);
            assertEquals(task.getTitle(), updatedTask.getTitle());
            assertEquals(task.getDescription(), updatedTask.getDescription());
            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verify(taskMapper, times(1)).updateTask(argThat(t -> t.getId().equals(updatedTask.getId())));

        }

        @Test
        @DisplayName("Should throw NotFound exception when user does not exist")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist(){

            final String userId = user.getId();

            when(userMapper.getUserByIdentifier(userId)).thenReturn(null);
            final NotFound exception = assertThrows(NotFound.class, () -> taskServiceImpl.updateTask(task));

            assertEquals("User with this identifier " + userId + " not found", exception.getMessage());

            verify(userMapper, times(1)).getUserByIdentifier(userId);
            verifyNoInteractions(taskMapper);
        }
    }

}