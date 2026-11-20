package hexlet.code.app;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskPatchDTO;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private JWTUtils jwtUtils;

    private Task testTask;
    private String token;

    @BeforeEach
    void setUp() {
        var user = userRepository.findById(1L).get();
        token = jwtUtils.generateToken(user.getUsername());

        testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> "Test task")
                .supply(Select.field(Task::getDescription), () -> "Description of test task")
                .supply(Select.field(Task::getIndex), () -> 954)
                .supply(Select.field(Task::getTaskStatus), () -> taskStatusRepository.findById(1L).get())
                .supply(Select.field(Task::getAssignee), () -> user)
                .create();

        testTask = taskRepository.save(testTask);
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token));

        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
        assertThatJson(body).isArray().hasSize(1);

        // Добавлен цикл for на случай, если в следующих задачах будет написание TaskInitializer
        var rootNode = objectMapper.readTree(body);
        boolean taskFound = false;
        for (var taskNode : rootNode) {
            if (taskNode.get("title").asString().equals(testTask.getName())) {
                taskFound = true;
                assertThat(taskNode.get("content").asString()).isEqualTo(testTask.getDescription());
                assertThat(taskNode.get("index").asInt()).isEqualTo(testTask.getIndex());
                assertThat(taskNode.get("assignee_id").asLong()).isEqualTo(testTask.getAssignee().getId());
                assertThat(taskNode.get("status").asString()).isEqualTo(testTask.getTaskStatus().getSlug());
            }
        }
        assertThat(taskFound).isTrue();
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setName(testTask.getName());
        dto.setDescription(testTask.getDescription());
        dto.setIndex(testTask.getIndex());
        dto.setAssigneeId(testTask.getAssignee().getId());
        dto.setTaskStatus(testTask.getTaskStatus().getSlug());

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isConflict()).andReturn();

        dto.setName("Test task2");
        var request2 = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request2).andExpect(status().isCreated()).andReturn();
        var savedTask = taskRepository.findByName(dto.getName()).get();

        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getName()).isEqualTo(dto.getName()).isNotEqualTo(testTask.getName());
        assertThat(savedTask.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(savedTask.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(savedTask.getAssignee().getId()).isEqualTo(testTask.getAssignee().getId());
        assertThat(savedTask.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
    }

    @Test
    public void testUpdate() throws Exception {
        var dto = new TaskPatchDTO();
        dto.setAssigneeId(JsonNullable.of(testTask.getAssignee().getId()));
        dto.setTaskStatus(JsonNullable.of(testTask.getTaskStatus().getSlug()));
        dto.setName(JsonNullable.of(testTask.getName()));
        dto.setDescription(JsonNullable.of(testTask.getDescription()));
        dto.setIndex(JsonNullable.of(testTask.getIndex()));

        var request = patch("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var newTask = taskRepository.findById(testTask.getId()).get();
        assertThat(newTask.getName()).isEqualTo(dto.getName().get());
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token));

        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body)
                .and(
                        v -> v.node("title").isEqualTo(testTask.getName()),
                        v -> v.node("id").isEqualTo(testTask.getId()),
                        v -> v.node("content").isEqualTo(testTask.getDescription()),
                        v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                        v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                        v -> v.node("index").isEqualTo(testTask.getIndex()),
                        v -> v.node("createdAt").isEqualTo(testTask.getCreatedAt())
                );
    }

    @Test
    public void testDelete() throws Exception {
        var request = delete("/api/tasks/" + testTask.getId())
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isNoContent()).andReturn();
        assertThat(taskRepository.existsById(testTask.getId())).isFalse();

        var request2 = delete("/api/tasks/" + testTask.getId())
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request2).andExpect(status().isNotFound()).andReturn();
    }
}
