package com.agriself.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          CustomAuthenticationSuccessHandler successHandler,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          RateLimitingFilter rateLimitingFilter) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Rate Limiting Protection (Anti-Brute Force)
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)

            // 2. Strict Security Headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny()) // Clickjacking protection
                .contentTypeOptions(content -> {}) // MIME-sniffing prevention (nosniff)
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
                    "font-src 'self' https://fonts.gstatic.com data:; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self'; " +
                    "frame-ancestors 'none';"
                ))
            )

            // 3. CSRF Protection (Enabled for web forms, ignored for public REST diagnostic demo API)
            .csrf(csrf -> {
                csrf.ignoringRequestMatchers("/api/v1/diagnostic/**");
            })

            // 4. Strict Role-Based Access Control (RBAC)
            .authorizeHttpRequests(auth -> auth
                // Public Assets, Sample Leaves & AI Diagnostic Demo Routes
                .requestMatchers(
                    "/", "/login", "/register",
                    "/css/**", "/js/**", "/images/**", "/sample_data/**", "/favicon.ico",
                    "/access-denied", "/error",
                    "/api/v1/diagnostic/**"
                ).permitAll()

                // Administrator Monitoring Routes (Strictly Company Admin granted via DB)
                .requestMatchers("/admin/**", "/api/admin/**").hasAuthority("ROLE_ADMIN")

                // Farmer Field & Telemetry Routes
                .requestMatchers("/farmer/**", "/api/farmer/**").hasAuthority("ROLE_FARMER")

                // Businessman / Consumer Market Routes
                .requestMatchers("/market/**", "/api/market/**").hasAuthority("ROLE_BUSINESSMAN")

                // Any other request must be fully authenticated
                .anyRequest().authenticated()
            )

            // 5. Form Login Configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )

            // 6. Logout Configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // 7. Access Denied Handling
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
            )

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
