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
						.requestMatchers("/", "/users/**").authenticated() // ユーザー選択・旅行一覧はログイン必須
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login") // カスタムログインページ
						.defaultSuccessUrl("/", true) // ログイン後はユーザー選択ページ
						.permitAll())
				.logout(logout -> logout
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.httpBasic(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable()); // 一時的に CSRF 無効化（確認用）
		return http.build();
	}
}
