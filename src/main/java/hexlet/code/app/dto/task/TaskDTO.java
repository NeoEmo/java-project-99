package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskDTO {

    private Long id;

    @JsonProperty("title")
    private String name;

    private Integer index;

    @JsonProperty("content")
    private String description;

    @JsonProperty("status")
    private String taskStatusSlug;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
}
