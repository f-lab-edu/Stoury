package com.stoury.config.websocket;

import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.service.ChatService;
import com.stoury.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuthorizationChannelInterceptor implements ChannelInterceptor {
    private final JwtUtils jwtUtils;
    private final ChatService chatService;
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (!StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        String token = accessor.getFirstNativeHeader("Authorization");

        if (StringUtils.hasText(token)) {
            checkRoomMember(token);
            return message;
        }
        throw new NotAuthorizedException();
    }

    private void checkRoomMember(String token) {
        Long memberId = jwtUtils.getMemberId(token);
        Long chatRoomId = jwtUtils.getChatRoomId(token);

        chatService.checkIfRoomMember(memberId, chatRoomId);
    }
}
