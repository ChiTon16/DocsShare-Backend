package com.tonz.tonzdocs.chat;

import com.tonz.tonzdocs.config.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.List;

/**
 * Đọc JWT trong header Authorization khi STOMP CONNECT,
 * parse lấy email bằng JwtUtil và gán vào accessor.setUser(new StompPrincipal(email)).
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwt;

    public StompAuthChannelInterceptor(JwtUtil jwt) { this.jwt = jwt; }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        StompCommand cmd = acc.getCommand();

        if (StompCommand.CONNECT.equals(cmd)) {
            String bearer = acc.getFirstNativeHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) {
                try {
                    String email = jwt.extractEmailFromAccess(bearer.substring(7));
                    var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
                    acc.setUser(auth);
                    acc.getSessionAttributes().put("email", email);
                    acc.setLeaveMutable(true);
                } catch (Exception ignore) {
                    // KHÔNG throw, KHÔNG return null → cho phép anonymous connect
                }
            }
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(cmd) || StompCommand.SEND.equals(cmd)) {
            if (acc.getUser() == null) {
                Object email = acc.getSessionAttributes().get("email");
                if (email != null) {
                    var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
                    acc.setUser(auth);
                    acc.setLeaveMutable(true);
                }
            }
            return message; // đừng chặn
        }

        return message;
    }
}
