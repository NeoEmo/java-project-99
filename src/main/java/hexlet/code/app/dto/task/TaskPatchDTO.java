package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskPatchDTO {

    private JsonNullable<Integer> index;

    @JsonAlias("assignee_id")
    private JsonNullable<Long> assigneeId;

    @JsonAlias("title")
    private JsonNullable<String> name;

    @JsonAlias("content")
    private JsonNullable<String> description;

    @JsonAlias("status")
    private JsonNullable<String> taskStatus;

    @JsonAlias("taskLabelIds")
    private JsonNullable<Set<Long>> labels;
}
