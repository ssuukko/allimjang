package com.alrimjang.config;

import com.alrimjang.service.ChatRoomAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class ChatStompAccessInterceptor implements ChannelInterceptor {

    private final ChatRoomAccessService chatRoomAccessService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command != StompCommand.SUBSCRIBE) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/chat.")) {
            return message;
        }

        Principal user = accessor.getUser();
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        String roomId = destination.substring("/topic/chat.".length());
        chatRoomAccessService.assertAccessible(roomId, user.getName());
        return message;
    }
}
