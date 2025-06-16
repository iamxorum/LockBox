package com.lockbox.config;

import com.lockbox.security.CsrfAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final CsrfAccessDeniedHandler csrfAccessDeniedHandler;

    public SecurityConfig(PasswordEncoder passwordEncoder, 
                        UserDetailsService userDetailsService,
                        CsrfAccessDeniedHandler csrfAccessDeniedHandler) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.csrfAccessDeniedHandler = csrfAccessDeniedHandler;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        var builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
               .passwordEncoder(passwordEncoder);
        return builder.build();
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        // For H2 Console
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/h2-console/**"),
                    new AntPathRequestMatcher("/api/**"),
                    new AntPathRequestMatcher("/actuator/**")
                ));
                
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        
        // Permit H2 Console and Swagger UI
        http.authorizeHttpRequests(auth -> auth
            // Static resources permissions
            .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
            // Microservices endpoints
            .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/health/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/eureka/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/public/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/microservices/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/passwords/new")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/passwords/edit/**")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/passwords/**")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/passwords/user/**")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/users/*/current-password")).hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()
            .anyRequest().authenticated()
        );
        
        // Configure form login with better success/failure handlers
        http.formLogin(formLogin -> formLogin
            .loginPage("/login")
            .permitAll()
            .defaultSuccessUrl("/dashboard", true)
            .failureUrl("/login?error=true")
        );
        
        // Configure logout
        http.logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/login?logout=true")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .permitAll()
        );
        
        // Configure custom access denied handler
        http.exceptionHandling(exceptionHandling -> 
            exceptionHandling.accessDeniedHandler(csrfAccessDeniedHandler));
        
        return http.build();
    }
    
    @Bean
    @Profile("!dev")
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/api/**"),
                    new AntPathRequestMatcher("/actuator/**")
                ));
                
        http.authorizeHttpRequests(auth -> auth
            // Static resources permissions
            .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
            // Microservices endpoints
            .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/health/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/eureka/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/public/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/microservices/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/passwords/new")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/passwords/edit/**")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/passwords/**")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/passwords/user/**")).authenticated()
            .requestMatchers(new AntPathRequestMatcher("/users/*/current-password")).hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()
            .anyRequest().authenticated()
        );
        
        // Configure form login with better success/failure handlers
        http.formLogin(formLogin -> formLogin
            .loginPage("/login")
            .permitAll()
            .defaultSuccessUrl("/dashboard", true)
            .failureUrl("/login?error=true")
        );
        
        // Configure logout
        http.logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/login?logout=true")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .permitAll()
        );
        
        // Configure custom access denied handler
        http.exceptionHandling(exceptionHandling -> 
            exceptionHandling.accessDeniedHandler(csrfAccessDeniedHandler));
        
        return http.build();
    }
} 