package com.example.extra.Controllers;

import com.example.extra.Entities.Task;
import com.example.extra.Services.Impl.TaskServiceImpl;
import com.example.extra.Success.Success;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    TaskServiceImpl taskServiceImpl;

    // Returns tasks linked to the currently logged-in user (either as client or provider).
    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<?> getMyJobs(Principal principal) {
        return Success.OK("Successfully Retrieved tasked linked to the currently logged-in user", taskServiceImpl.getAllTasks(principal.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<?> getTaskById(@PathVariable String id) {
        return Success.OK("Successfully retrieved task details", taskServiceImpl.getTaskById(id));
    }

    // Creates a new service request (Status: PENDING).
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createTask(@Valid @RequestBody Task task) {
        return Success.OK("Successfully created task", taskServiceImpl.createTask(task));
    }

    // Creates a new service request (Status: PENDING).
    @PatchMapping("/update")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> updateTask(@Valid @RequestBody Task task) {
        return Success.OK("Successfully created task", taskServiceImpl.updateTask(task));
    }

    // Assigns the authenticated provider's ID to the task and updates status to ACCEPTED.
    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> acceptTask(@PathVariable String id, Principal principal){
        return Success.OK("Successfully accepted task", taskServiceImpl.acceptTask(id, principal.getName()));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> startTask(@PathVariable String id, Principal principal){
        return Success.OK("Successfully started task", taskServiceImpl.startTask(id, principal.getName()));
    }

    @PatchMapping("{id}/complete-request")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> taskPendingConfirmation(@PathVariable String id, Principal principal){
        return Success.OK("Successfully triggered completion request", taskServiceImpl.taskPendingConfirmation(id, principal.getName()));
    }

    @PatchMapping("/{id}/approve-completion")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> approveTaskCompletion(@PathVariable String id, Principal principal){
        return Success.OK("Successfully approved task completion", taskServiceImpl.approveTaskCompletion(id, principal.getName()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<?> cancelTask(@PathVariable String id, Principal principal){
        return Success.OK("Successfully cancelled task", taskServiceImpl.cancelTask(id, principal.getName()));
    }

}
