package com.primevideo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ProfileInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Ne pas intercepter les ressources statiques, l'auth, ou la page de sélection elle-même
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images") || 
            uri.startsWith("/media") || uri.startsWith("/auth") || uri.startsWith("/profiles/select") || 
            uri.startsWith("/profiles/create") || uri.startsWith("/api")) {
            return true;
        }

        HttpSession session = request.getSession();
        if (request.getUserPrincipal() != null && session.getAttribute("selectedProfile") == null) {
            response.sendRedirect("/profiles/select");
            return false;
        }

        return true;
    }
}
