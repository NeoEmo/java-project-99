package hexlet.code.app;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelPatchDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
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
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    private Label testLabel;
    private String token;

    @BeforeEach
    void setUp() {
        var user = userRepository.findById(1L).get();
        token = jwtUtils.generateToken(user.getUsername());

        testLabel = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getTasks))
                .supply(Select.field(Label::getName), () -> "Test label")
                .create();

        testLabel = labelRepository.save(testLabel);
    }

    @Test
    public void testIndex()  throws Exception {
        var request = get("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.add("Authorization", "Bearer " + token));

        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
        assertThatJson(body).isArray().hasSize(3);

        var rootNode = objectMapper.readTree(body);
        boolean labelFound = false;
        for (var labelNode : rootNode) {
            if (labelNode.get("name").asString().equals(testLabel.getName())) {
                labelFound = true;
            }
        }
        assertThat(labelFound).isTrue();
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName(testLabel.getName());

        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isConflict()).andReturn();

        dto.setName("Test label2");
        var request2 = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request2).andExpect(status().isCreated()).andReturn();
        var savedLabel = labelRepository.findByName(dto.getName()).get();

        assertThat(savedLabel).isNotNull();
        assertThat(savedLabel.getName()).isEqualTo(dto.getName()).isNotEqualTo(testLabel.getName());
    }

    @Test
    public void testUpdate() throws Exception {
        var dto = new LabelPatchDTO();
        dto.setName(JsonNullable.of("Test label2"));

        var request = patch("/api/labels/" + testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.add("Authorization", "Bearer " + token))
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var newLabel = labelRepository.findByName(dto.getName().get()).get();
        assertThat(newLabel.getName()).isEqualTo(dto.getName().get());
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/labels/" + testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.add("Authorization", "Bearer " + token));

        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body)
                .and(
                        v -> v.node("name").isEqualTo(testLabel.getName())
                );
    }

    @Test
    public void testDelete() throws Exception {
        var request = delete("/api/labels/" + testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.add("Authorization", "Bearer " + token));

        mockMvc.perform(request).andExpect(status().isNoContent()).andReturn();
        assertThat(labelRepository.existsById(testLabel.getId())).isFalse();

        mockMvc.perform(request).andExpect(status().isNotFound()).andReturn();
    }
}
