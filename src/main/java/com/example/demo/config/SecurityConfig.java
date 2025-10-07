package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig {

	// パスワードのハッシュ化方式
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// UserDetailsServiceImpl を Bean 化して、UserRepository を注入
	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return new UserDetailsServiceImpl(userRepository);
	}

	// 認証プロバイダ設定
	@Bean
	public DaoAuthenticationProvider authenticationProvider(UserRepository userRepository) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService(userRepository));
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	// セキュリティ設定
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
						.requestMatchers("/login", "/signup").permitAll()
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/mypage", true) // ✅ ログイン後にマイページへ
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.httpBasic(Customizer.withDefaults());

		return http.build();
	}
}
