package com.alrimjang.config;

import com.alrimjang.service.ActiveUserTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityActivityListener {

    private final ActiveUserTracker activeUserTracker;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication != null) {
            activeUserTracker.markActive(authentication.getName());
        }
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication != null) {
            activeUserTracker.markInactive(authentication.getName());
        }
    }

    @EventListener
    public void onSessionDestroyed(SessionDestroyedEvent event) {
        for (SecurityContext context : event.getSecurityContexts()) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null) {
                activeUserTracker.markInactive(authentication.getName());
            }
        }
    }
}
