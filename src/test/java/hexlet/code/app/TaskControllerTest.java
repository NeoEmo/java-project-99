package hexlet.code.app;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskParamsDTO;
import hexlet.code.app.dto.task.TaskPatchDTO;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.utils.JWTUtils;
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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
public class TaskControllerTest extends TestInitializer {

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
    private LabelRepository labelRepository;

    @Autowired
    private JWTUtils jwtUtils;

    private Task testTask;
    private String token;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
        labelRepository.deleteAll();

        super.build();

        var user = userRepository.findByEmail("hexlet@example.com").get();
        token = jwtUtils.generateToken(user.getUsername());

        testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> "Test task")
                .supply(Select.field(Task::getDescription), () -> "Description of test task")
                .supply(Select.field(Task::getIndex), () -> 954)
                .supply(Select.field(Task::getTaskStatus), () -> taskStatusRepository.findByName("Draft").get())
                .supply(Select.field(Task::getAssignee), () -> user)
                .supply(Select.field(Task::getLabels), () -> new HashSet<>(
                        Set.of(labelRepository.findByName("feature").get())))
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
        request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isCreated()).andReturn();
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
        dto.setName(JsonNullable.of("Test task2"));
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

    @Test
    public void testUpdateLabel() throws Exception {
        var dto = new TaskPatchDTO();
        dto.setLabels(JsonNullable.of(new HashSet<>(Set.of(labelRepository.findAll()
                .stream().map(l -> l.getId()).toArray(Long[]::new)))));

        var request = patch("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var newTask = taskRepository.findById(testTask.getId()).get();
        assertThat(newTask.getLabels().size()).isEqualTo(2);
        assertThat(newTask.getLabels().contains(dto.getLabels().get()));

        dto.setLabels(JsonNullable.of(Set.of()));

        var request2 = patch("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request2).andExpect(status().isOk()).andReturn();
        newTask = taskRepository.findById(testTask.getId()).get();
        assertThat(newTask.getLabels().size()).isEqualTo(0);
        assertThat(newTask.getLabels().contains(dto.getLabels().get()));
    }

    @Test
    public void testCreateLabel() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setLabels(new HashSet<Long>(Set.of(labelRepository.findAll()
                .stream().map(l -> l.getId()).toArray(Long[]::new))));
        dto.setName("Test task2");
        dto.setTaskStatus(taskStatusRepository.findBySlug("draft").get().getSlug());

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isCreated()).andReturn();

            var newTask = taskRepository.findByName(dto.getName()).get();
        assertThat(newTask.getLabels().size()).isEqualTo(2);

        assertThat(newTask.getLabels().stream().map(l -> l.getId()).collect(Collectors.toSet()))
                .isEqualTo(dto.getLabels());
    }

    @Test
    public void testFilterIndex() throws Exception {
        // Специально создаём второй Task, который не подходит под все фильтры
        var dto = new TaskCreateDTO();
        dto.setIndex(343);
        dto.setAssigneeId(userRepository.findByEmail("hexlet@example.com").get().getId());
        dto.setName("sadsddfgdf");
        dto.setTaskStatus("to_review");
        dto.setLabels(Set.of(labelRepository.findByName("bug").get().getId()));

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isCreated()).andReturn();

        request = get("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token));

        var subResult = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var subBody = subResult.getResponse().getContentAsString();

        assertThatJson(subBody).isArray();
        assertThatJson(subBody).isArray().hasSize(2);

        var params = new TaskParamsDTO();
        params.setAssigneeId(userRepository.findByEmail("hexlet@example.com").get().getId());
        params.setLabelId(labelRepository.findByName("feature").get().getId());
        params.setStatus("draft");
        params.setTitleCont("Test");

        var request2 = get("/api/tasks")
                .headers(httpHeaders ->  httpHeaders.add("Authorization", "Bearer " + token))
                .contentType(MediaType.APPLICATION_JSON)
                .param("titleCont", params.getTitleCont())
                .param("assigneeId", params.getAssigneeId().toString())
                .param("status", params.getStatus())
                .param("labelId", params.getLabelId().toString());

        var result = mockMvc.perform(request2).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
        assertThatJson(body).isArray().hasSize(1);

        var rootNode = objectMapper.readTree(body);
        boolean taskFound = false;
        for (var taskNode : rootNode) {
            if (taskNode.get("title").asString().contains(params.getTitleCont())) {
                taskFound = true;
                assertThat(taskNode.get("assignee_id").asLong()).isEqualTo(params.getAssigneeId());
                assertThat(taskNode.get("taskLabelIds").asArray().get(0).asLong()).isEqualTo(params.getLabelId());
                assertThat(taskNode.get("status").asString()).isEqualTo(params.getStatus());
            }
        }
        assertThat(taskFound).isTrue();
    }
}
