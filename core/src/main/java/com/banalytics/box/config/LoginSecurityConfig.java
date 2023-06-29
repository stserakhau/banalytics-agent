package com.banalytics.box.config;

import com.banalytics.box.service.security.SingleUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsUtils;

/**
 * https://docs.spring.io/spring-security/site/docs/5.2.12.RELEASE/reference/html/oauth2.html
 * <p>
 * <p>
 * https://www.baeldung.com/spring-security-authenticationmanagerresolver
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Slf4j
public class LoginSecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private SingleUserDetailsService singleUserDetailsService;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().disable().and()
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/public/**", "/api/public/**", "/static/**", "/#/**").permitAll()
                .antMatchers("/secured/**", "/api/secured/**").authenticated()
                .and()
                .userDetailsService(singleUserDetailsService)
                .formLogin()
                .loginPage("/index.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/secured/index.html", true)
                .failureUrl("/index.html?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessUrl("/");

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PASSWORD_ENCODER;
    }
}