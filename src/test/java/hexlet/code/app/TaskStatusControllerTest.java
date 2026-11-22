package hexlet.code.app;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusPatchDTO;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.utils.JWTUtils;
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
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtils jwtUtils;

    private TaskStatus testTaskStatus;
    private String token;

    @BeforeEach
    void setUp() {
        testTaskStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> "Test status")
                .supply(Select.field(TaskStatus::getSlug), () -> "test_status")
                .create();

        testTaskStatus = taskStatusRepository.save(testTaskStatus);
        var testUser = userRepository.findById(1L).get();
        token = jwtUtils.generateToken(testUser.getUsername());
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/task_statuses")).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        // Проверяем, что вообще получаем JSON типа [{Данные1}, {Данные2}]
        assertThatJson(body).isArray();
        assertThatJson(body).isArray().hasSize(6);

        var rootNode = objectMapper.readTree(body);
        boolean taskStatusFound = false;
        for (var taskStatusNode : rootNode) {
            if (taskStatusNode.get("name").asString().equals(testTaskStatus.getName())) {
                taskStatusFound = true;
                assertThat(taskStatusNode.get("slug").asString()).isEqualTo(testTaskStatus.getSlug());
            }
        }
        assertThat(taskStatusFound).isTrue();
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setName(testTaskStatus.getName());
        dto.setSlug(testTaskStatus.getSlug());

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isConflict()).andReturn();

        dto.setSlug("test_status2");
        var request2 = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request2).andExpect(status().isCreated()).andReturn();
        var savedTaskStatus = taskStatusRepository.findBySlug(dto.getSlug()).get();
        assertThat(savedTaskStatus).isNotNull();
        assertThat(savedTaskStatus.getName())
                .isEqualTo(testTaskStatus.getName())
                .isEqualTo(dto.getName());
    }

    @Test
    public void testUpdate() throws Exception {
        var dto = new TaskStatusPatchDTO();
        dto.setName(JsonNullable.of(testTaskStatus.getName()));
        dto.setSlug(JsonNullable.of(testTaskStatus.getSlug()));

        var request = patch("/api/task_statuses/" + testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isOk()).andReturn();

        var newTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).get();
        assertThat(newTaskStatus.getName())
                .isEqualTo(dto.getName().get())
                .isEqualTo(testTaskStatus.getName());

        assertThat(newTaskStatus.getSlug())
                .isEqualTo(dto.getSlug().get())
                .isEqualTo(testTaskStatus.getSlug());
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/task_statuses/" + testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON);

        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body)
                .and(
                        v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                        v -> v.node("slug").isEqualTo(testTaskStatus.getSlug())
                );
    }

    @Test
    public void testDelete() throws Exception {
        var request = delete("/api/task_statuses/" + testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(testTaskStatus.getId()));

        mockMvc.perform(request).andExpect(status().isNoContent()).andReturn();
        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isFalse();

        var request2 = delete("/api/task_statuses/" + testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(testTaskStatus.getId()));

        mockMvc.perform(request2).andExpect(status().isNotFound()).andReturn();
    }
}
