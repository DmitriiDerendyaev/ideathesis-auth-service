package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void testPasswordMatch() {
		String rawPassword = "testpass";
		String encodedPassword = "$2a$10$sjfG2lpmM6/0d78Ot3x9s.BQztadXkz21EolVqtKpdBfZczImXldi";

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
		boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

		assertTrue(matches); // Должно быть true
	}

	@Test
	public void testPasswordHashing() {
		String rawPassword = "testpass";
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

		// Создаем новый хеш
		String newEncodedPassword = passwordEncoder.encode(rawPassword);
		System.out.println("New encoded password: " + newEncodedPassword);

		// Проверяем, совпадает ли новый хеш с текущим
		boolean matches = passwordEncoder.matches(rawPassword, "$2a$10$sjfG2lpmM6/0d78Ot3x9s.BQztadXkz21EolVqtKpdBfZczImXldi");
		assertTrue(matches);
	}

}
