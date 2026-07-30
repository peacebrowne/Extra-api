package com.example.extra.Interceptors;

import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Services.Impl.AuthenticationServiceImpl;
import com.example.extra.Services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UsernameChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final AuthenticationServiceImpl authenticationServiceImpl;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");

            try {
                if (authorization != null && authorization.startsWith("Bearer ")) {
                    String token = authorization.substring(7);

                    String userEmail = jwtService.extractEmail(token);
                    UserDetails userDetails = authenticationServiceImpl.loadUserByUsername(userEmail);
                    if (jwtService.isTokenExpired(token) || jwtService.validateToken(token, userDetails)
                    ) {
                        accessor.setUser(()-> userEmail);

                        log.info("Logged in as {}", userEmail);
                    }
                }

            }catch (BadRequest e){
                log.error("Wrong user trying login: {}", e.getMessage());
                throw new BadRequest("Invalid token");
            }catch (Exception e){
                log.error("WebSocket authentication failed! Dropping connection. Error: {}", e.getMessage());
                throw new InternalServerError("Invalid token", e);
            }

        }

        return message;
    }

}
