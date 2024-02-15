package com.stoury.config;

import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.lang.reflect.Method;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Slf4j
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${origin}")
    private String origin;

    private final JwtUtils jwtUtils;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*", origin)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(argumentResolver());
        WebSocketMessageBrokerConfigurer.super.addArgumentResolvers(argumentResolvers);
    }

    @Bean
    public HandlerMethodArgumentResolver argumentResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                Method method = parameter.getMethod();
                return method != null
                        &&  method.isAnnotationPresent(MessageMapping.class)
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
        };
    }
}
