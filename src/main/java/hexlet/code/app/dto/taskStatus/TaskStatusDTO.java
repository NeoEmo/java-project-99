package hexlet.code.app.dto.taskStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskStatusDTO {
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String slug;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
}
