package com.smartqueue.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String redirectUrl = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter("ROLE_ADMIN"::equals)
                .findFirst()
                .map(role -> "/admin/dashboard")
                .orElseGet(() -> authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter("ROLE_STAFF"::equals)
                        .findFirst()
                        .map(role -> "/staff/dashboard")
                        .orElse("/user/dashboard"));

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}
