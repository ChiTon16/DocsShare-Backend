package com.tonz.tonzdocs.chat;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor interceptor;

    public WebSocketSecurityConfig(StompAuthChannelInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    /** 1) Đăng ký endpoint SockJS cho FE */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173") // domain FE dev
                .withSockJS(); // nếu FE dùng SockJS
        // Nếu muốn WS thuần: bỏ .withSockJS() (FE dùng brokerURL ws://.../ws)
    }

    /** 2) Bật message broker và prefix */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");      // nơi FE subscribe
        registry.setApplicationDestinationPrefixes("/app"); // nơi FE publish
        // registry.setUserDestinationPrefix("/user"); // nếu cần gửi riêng từng user
    }

    /** 3) Giữ interceptor để kiểm JWT trong CONNECT/SEND */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(interceptor);
    }
}
