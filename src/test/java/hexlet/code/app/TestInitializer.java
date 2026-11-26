package hexlet.code.app;

import hexlet.code.app.model.Label;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootTest
class TestInitializer {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private LabelRepository labelRepository;

	@Autowired
	private TaskStatusRepository taskStatusRepository;

	public void build() {
		createUser();
		createLabels();
		createTaskStatuses();
	}

	// В теории, заменить на сервис и в CommandLiner заменить всё на одну строчку
	public void createUser() {
		User admin = new User();
		admin.setEmail("hexlet@example.com");
		admin.setFirstName("Admin");
		admin.setLastName("Kirill");
		admin.setPasswordDigest(passwordEncoder.encode("qwerty"));

		userRepository.save(admin);
		System.out.println("Админы успешно добавлены в бд!");
	}

	private void createLabels() {
		var feature = new Label();
		feature.setName("feature");

		var bug = new Label();
		bug.setName("bug");

		labelRepository.saveAll(List.of(feature, bug));

		System.out.println("Дефолтные метки были успешно добавлены");
	}

	private void createTaskStatuses() {
		var draft = new TaskStatus();
		draft.setName("Draft");
		draft.setSlug("draft");

		var toReview = new TaskStatus();
		toReview.setName("ToReview");
		toReview.setSlug("to_review");

		var toBeFixed = new TaskStatus();
		toBeFixed.setName("ToBeFixed");
		toBeFixed.setSlug("to_be_fixed");

		var toPublish = new TaskStatus();
		toPublish.setName("ToPublish");
		toPublish.setSlug("to_publish");

		var published = new TaskStatus();
		published.setName("Published");
		published.setSlug("published");

		taskStatusRepository.saveAll(List.of(draft, toReview, toBeFixed, toPublish, published));

		System.out.println("Дефолтные статусы задач успешно добавлены в репозиторий!");
	}
}
