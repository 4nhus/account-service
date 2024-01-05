package account.security;

import account.user.AccountUser;
import account.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
    private final UserRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public SecurityConfig(UserRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(auth -> auth.authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
                            String user = new String(Base64.getDecoder().decode(request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1]), StandardCharsets.UTF_8).split(":")[0];

                            boolean isUserLocked = repository.findUserByEmailIgnoreCase(user).isPresent() && repository.findUserByEmailIgnoreCase(user).get().isLocked();
                            if (!isUserLocked) {
                                eventPublisher.publishEvent(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.LOGIN_FAILED, user, request.getServletPath(), request.getServletPath()));
                            }
                        }

                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                        final Map<String, Object> body = new HashMap<>();
                        body.put("error", "Unauthorized");
                        body.put("path", request.getServletPath());
                        body.put("status", response.getStatus());
                        body.put("message", "User account is locked");
                        new ObjectMapper().writeValue(response.getOutputStream(), body);
                    }
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/security/events/").hasRole("AUDITOR")
                        .requestMatchers("/api/admin/user/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/actuator/shutdown").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass").hasAnyRole("ADMINISTRATOR", "ACCOUNTANT", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyRole("ACCOUNTANT", "USER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, accessDeniedException) -> {
                    eventPublisher.publishEvent(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.ACCESS_DENIED, request.getUserPrincipal().getName(), request.getRequestURI(), request.getRequestURI()));
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
                }))
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            AccountUser user = repository.findUserByEmailIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return User.withUsername(user.getEmail()).password(user.getPassword()).roles(user.getRolesAsStrings()).disabled(user.isLocked() && !user.isAdmin()).build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }
}
