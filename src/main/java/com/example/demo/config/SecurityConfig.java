package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 認証情報（ユーザー名・パスワード）
    @Bean
    public UserDetailsService users() {
        UserDetails user = User.builder()
            .username("syouki")
            .password("{noop}syouki0618") // {noop}は平文パスワード用
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    // セキュリティ設定
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")          
                .defaultSuccessUrl("/trips", true) 
                .permitAll()
            )
            .logout(logout -> logout.permitAll())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
