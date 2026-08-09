package com.example.extra.Services;

import com.example.extra.Entities.Task;
import com.example.extra.Enumerator.TaskStatus;

import java.util.List;

public interface TaskService {
     Task createTask(Task task, String identifier);
     Task updateTask(String id, Task task, String identifier);
     Task deleteTask(String id, String identifier);
     Task getTaskById(String id);
     List<Task> getAllTasks(String identifier);
     Task acceptTask(String id, String identifier);
     Task startTask(String id, String identifier);
     Task taskPendingConfirmation(String id, String identifier);
     Task approveTaskCompletion(String id, String identifier);
     Task cancelTask(String id, String identifier);
     List<Task> getInProgressTasks(String identifier);
     List<Task> searchUserTask(String identifier, String term, TaskStatus status);
}
