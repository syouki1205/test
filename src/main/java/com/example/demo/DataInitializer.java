package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner createDefaultUser(UserRepository userRepository) {
		return args -> {
			String defaultUsername = "syouki"; // 作りたいユーザー名
			String defaultPassword = "syouki0618"; // 作りたいパスワード
			String defaultEmail = "admin@example.com";

			// 既に同じユーザー名があれば作らない
			if (userRepository.findByUsername(defaultUsername).isEmpty()) {
				BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
				User user = new User();
				user.setUsername(defaultUsername);
				user.setPassword(encoder.encode(defaultPassword)); // BCryptでハッシュ化
				user.setEmail(defaultEmail);
				userRepository.save(user);
				System.out.println("Default user created: " + defaultUsername);
			} else {
				System.out.println("Default user already exists: " + defaultUsername);
			}
		};
	}
}
