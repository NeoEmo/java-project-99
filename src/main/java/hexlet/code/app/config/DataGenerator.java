package hexlet.code.app.config;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataGenerator implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public DataGenerator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		if (userRepository.findByEmail("hexlet@example.com").isEmpty()) {
			var admin = new User();
			admin.setEmail("hexlet@example.com");
			admin.setFirstName("Admin");
			admin.setLastName("Kirill");
			admin.setPasswordDigest(passwordEncoder.encode("qwerty"));

			userRepository.save(admin);
			System.out.println("Админы успешно добавлены в бд!");
		}
	}
}
