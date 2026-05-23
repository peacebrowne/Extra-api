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
        return Success.OK("Successfully Retrieved tasked linked to the currently logged-in user",
                taskServiceImpl.getAllTasks(
                        principal.getName()
                ));
    }

    // Creates a new service request (Status: PENDING).
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createTask(@Valid @RequestBody Task task) {
        return Success.OK("Successfully created task", taskServiceImpl.createTask(task));
    }

    // Assigns the authenticated provider's ID to the task and updates status to ACCEPTED.
    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> acceptTask(@PathVariable String id, Principal principal){
        return Success.OK("Successfully accepted task", taskServiceImpl.acceptTask(id, principal.getName()));
    }




}
