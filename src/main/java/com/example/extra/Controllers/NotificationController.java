package com.example.extra.Controllers;

import com.example.extra.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.Notification;

import static com.aliyun.openapiutil.Client.getTimestamp;

@Slf4j
@RequiredArgsConstructor
@Controller
public class NotificationController {

    private final NotificationService notificationService;

    @MessageMapping("provider")
    @SendTo("/topic/provider-tasks")
    public void sendNotification(){
        notificationService.getLoggedInUsers();
    }

}
