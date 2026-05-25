package com.example.extra.Services;

import com.example.extra.Entities.Task;

import java.security.Principal;
import java.util.List;

public interface TaskService {
     Task createTask(Task task);
     Task updateTask(Task task);
     Task deleteTask(String id);
     Task getTaskById(String id);
     List<Task> getAllTasks(String email);
     Task acceptTask(String id, String email);
     Task startTask(String id, String email);
     Task taskPendingConfirmation(String id, String email);
     Task approveTaskCompletion(String id, String email);
     Task cancelTask(String id, String email);
}
