package com.example.extra.Services;

import com.example.extra.Entities.Task;

import java.util.List;

public interface TaskService {
     Task createTask(Task task);
     Task updateTask(Task task);
     Task deleteTask(String id);
     Task getTaskById(String id);
     List<Task> getAllTasks(String email);
     Task acceptTask(String taskId, String email);
}
