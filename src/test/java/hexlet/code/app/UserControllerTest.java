package hexlet.code.app;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserPatchDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private User testUser;

	@BeforeEach
	void setUp() throws Exception {
		testUser = Instancio.of(User.class)
				.ignore(Select.field(User::getId))
				.supply(Select.field(User::getEmail), () -> "example@hotmail.com")
				.supply(Select.field(User::getFirstName), () -> "John")
				.supply(Select.field(User::getLastName), () -> "Doe")
				.supply(Select.field(User::getPasswordDigest),
						() -> passwordEncoder.encode("1234567890a"))
				.create();

		testUser = userRepository.save(testUser);
	}

	@Test
	public void testIndex() throws Exception {
		var result = mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andReturn();
		var body = result.getResponse().getContentAsString();

		// Проверка на то, что мы вообще получаем Json
		assertThatJson(body).node("content").isArray();

		// поиск testUser и проверка его
		var rootNode = objectMapper.readTree(body);
		var contentArray = rootNode.get("content");
		boolean userFound = false;
		for (var userNode : contentArray) {
			if (userNode.get("email").asString().equals(testUser.getEmail())) {
				userFound = true;
				assertThat(userNode.get("firstName").asString()).isEqualTo(testUser.getFirstName());
				assertThat(userNode.get("lastName").asString()).isEqualTo(testUser.getLastName());
			}
		}
		assertThat(userFound).isTrue();
	}

	@Test
	public void testCreate() throws Exception {
		var dto = new UserCreateDTO();
		dto.setEmail(testUser.getEmail());
		dto.setFirstName(testUser.getFirstName());
		dto.setLastName(testUser.getLastName());
		dto.setPassword("1234567890a");

		var request = post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));

		mockMvc.perform(request).andExpect(status().isConflict()).andReturn();

		// Мы меняем только почту, однако если нужны будут другие проверки уникальности,
		// надо будет менять и другие значения.
		dto.setEmail("example2@hotmail.com");
		var request2 = post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));

		mockMvc.perform(request2).andExpect(status().isCreated()).andReturn();
		var savedUser = userRepository.findByEmail(dto.getEmail()).get();
		assertThat(savedUser).isNotNull();
		assertThat(savedUser.getEmail()).isEqualTo(dto.getEmail());

		assertThat(savedUser.getPasswordDigest()).isNotEqualTo(dto.getPassword());
		assertThat(passwordEncoder.matches(dto.getPassword(), savedUser.getPasswordDigest())).isTrue();
	}

	@Test
	public void testUpdate() throws Exception {
		var dto = new UserPatchDTO();
		// обновляем всё, хотя можно частично
		// Данный Email уже занят
		dto.setEmail(JsonNullable.of("hexlet@example.com"));

		// Не обязательные данные, но в случае чего можно просто менять Patch на Put и всё не сломается
		dto.setFirstName(JsonNullable.of(testUser.getFirstName()));
		dto.setLastName(JsonNullable.of(testUser.getLastName()));

		// ещё мы хотим поменять пароль
		dto.setPassword(JsonNullable.of("newPassword12345"));

		var request = patch("/api/users/" + testUser.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));

		mockMvc.perform(request).andExpect(status().isConflict()).andReturn();

		dto.setEmail(JsonNullable.of("hexlet123@example.com"));
		var request2 = patch("/api/users/" + testUser.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));

		mockMvc.perform(request2).andExpect(status().isOk()).andReturn();
		var newTestUser = userRepository.findById(testUser.getId()).get();
		assertThat(newTestUser.getEmail()).isEqualTo(dto.getEmail().get());
		assertThat(passwordEncoder.matches(dto.getPassword().get(), newTestUser.getPasswordDigest())).isTrue();
	}

	@Test
	public void testShow() throws Exception {
		var request = get("/api/users/" + testUser.getId())
				.contentType(MediaType.APPLICATION_JSON);

		var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
		var body = result.getResponse().getContentAsString();
		assertThatJson(body)
				.and(
						v -> v.node("email").isEqualTo(testUser.getEmail()),
						v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
						v -> v.node("lastName").isEqualTo(testUser.getLastName())
				);
	}

	@Test
	public void testDelete() throws Exception {
		var request = delete("/api/users/" + testUser.getId());
		mockMvc.perform(request).andExpect(status().isNoContent()).andReturn();
		assertThat(userRepository.existsById(testUser.getId())).isFalse();

		var request2 =  delete("/api/users/" + testUser.getId());
		mockMvc.perform(request2).andExpect(status().isNotFound()).andReturn();
	}
}
