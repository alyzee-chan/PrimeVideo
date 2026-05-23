package com.primevideo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration centrale de la sécurité Spring Security.
 *
 * NOTE ARCHITECTURE : le UserDetailsService a été extrait dans CustomUserDetailsService
 * pour éviter la dépendance circulaire :
 *   JwtFilter → UserDetailsService ← SecurityConfig   (pas de cycle)
 *
 * Avant (problème) :
 *   JwtFilter -> SecurityConfig -> JwtFilter  (CYCLE !)
 *
 * Après (solution) :
 *   JwtFilter -> CustomUserDetailsService  (pas de cycle)
 *   SecurityConfig -> JwtFilter + CustomUserDetailsService  (pas de cycle)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    // CustomUserDetailsService est un @Service indépendant — plus de cycle !
    private final CustomUserDetailsService userDetailsService;

    private static final String[] PUBLIC_URLS = {
        "/", "/home",
        "/auth/login", "/auth/register", "/auth/forgot-password", "/auth/reset-password",
        "/catalog", "/catalog/**",
        "/search/**",
        "/api/auth/**",
        "/api/v1/**",
        "/api/catalog/**",
        "/api/chatbot/**",
        "/css/**", "/js/**", "/images/**", "/img/**",
        "/media/**", "/static/**",
        "/subscriptions/**",
        "/error"
    };

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/media/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/img/**",
            "/static/**",
            "/favicon.ico"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers("/admin/**").access((authentication, context) -> {
                    String remoteAddr = context.getRequest().getRemoteAddr();
                    boolean isLocal = "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(remoteAddr);
                    boolean isAdmin = authentication.get().getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    return new org.springframework.security.authorization.AuthorizationDecision(isLocal && isAdmin);
                })
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/profiles/select", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
