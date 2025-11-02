package com.Rakumo.gateway.filter;

import com.Rakumo.gateway.service.GrpcAuthClientService;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final GrpcAuthClientService authClientService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Validate token using Auth service
                boolean isValid = authClientService.isValidToken(token);

                if (isValid) {
                    // Get user info from token
                    var userResponse = authClientService.getUserInfo(token);

                    // Additional security checks
                    if (!userResponse.getIsEnabled()) {
                        log.warn("Authentication attempted with disabled account: {}", userResponse.getEmail());
                        SecurityContextHolder.clearContext();
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Account disabled");
                        return;
                    }

                    // Create authentication object
                    List<SimpleGrantedAuthority> authorities = userResponse.getRolesList().stream()
                            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userResponse.getEmail(),
                                    null,
                                    authorities
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user: {} with roles: {}", userResponse.getEmail(), authorities);
                } else {
                    log.warn("Invalid JWT token received");
                    SecurityContextHolder.clearContext();
                }
            } catch (StatusRuntimeException e) {
                log.error("Auth service unavailable during token validation: {}", e.getStatus());
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
                return;
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
                status == 403 ? "Forbidden" : "Service Unavailable", message));
    }
}