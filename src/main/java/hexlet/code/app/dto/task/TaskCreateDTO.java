package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {

    private Integer index;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @JsonProperty("title")
    @NotBlank
    private String name;

    @JsonProperty("content")
    private String description;

    @JsonProperty("status")
    @NotNull
    private String taskStatus;

    @JsonProperty("taskLabelIds")
    private Set<Long> labels;
}
