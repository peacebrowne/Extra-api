package com.example.extra.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final SimpUserRegistry simpUserRegistry;

    public void getLoggedInUsers() {
        simpUserRegistry.getUsers().forEach(user -> {
            log.info("User {} logged in", user.getName());

        });

    }
}
