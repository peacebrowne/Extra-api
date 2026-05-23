package com.example.extra.Services.Impl;

import com.example.extra.Entities.Task;
import com.example.extra.Mappers.TaskMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    TaskMapper taskMapper;

    @InjectMocks
    TaskServiceImpl taskServiceImpl;

    private static Task task = null;

    @BeforeAll
    static void setUp() {
        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setClientId("123e4567-e89b-12d3-a456-426614174000d");
    }

    @BeforeEach
    public void setUpEachTest() {
       System.out.println("Before each test");
    }

    @Test
    void createTaskShouldCreateTaskSuccessfully() {

        Mockito.when(taskMapper.createTask(task)).thenReturn(task);

        Task createTask = taskServiceImpl.createTask(task);

        assertNotNull(createTask);
        assertEquals(task.getTitle(), createTask.getTitle());
        assertEquals(task.getDescription(), createTask.getDescription());
    }

    @Test
    void updateTaskShouldUpdateTaskSuccessfully() {
        task.setId("123e4567-e89b-12d3-a456-426614174000");

        Mockito.when(taskMapper.getTaskById(task.getId())).thenReturn(task);
        Mockito.doNothing().when(taskMapper).updateTask(task);

        Task updatedTask = taskServiceImpl.updateTask(task);
        assertEquals(task.getTitle(), updatedTask.getTitle());
        assertSame(task.getId(), updatedTask.getId());
    }

    @Test
    void deleteTaskShouldDeleteTaskSuccessfully() {
        String id = "123e4567-e89b-12d3-a456-426614174000";
        task.setId("123e4567-e89b-12d3-a456-426614174000");

        Mockito.when(taskMapper.getTaskById(id)).thenReturn(task);
        Mockito.doNothing().when(taskMapper).deleteTask(id);

        assertEquals(task, taskServiceImpl.deleteTask(id));

    }
}