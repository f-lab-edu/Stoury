package com.stoury.config.websocket;

import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class MemberIdArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtUtils jwtUtils;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Method method = parameter.getMethod();
        return method != null
                && method.isAnnotationPresent(MessageMapping.class)
                && "memberId".equals(parameter.getParameterName());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) {
        try {
            String token = StompHeaderAccessor.wrap(message).getFirstNativeHeader("Authorization");
            return jwtUtils.getMemberId(token);
        } catch (NullPointerException e) {
            throw new NotAuthorizedException("Message header should contain authorization token");
        }
    }
}
