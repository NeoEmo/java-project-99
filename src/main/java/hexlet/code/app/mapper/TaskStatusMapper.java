package hexlet.code.app.mapper;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusPathDTO;
import hexlet.code.app.model.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(uses = {ReferenceMapper.class, JsonNullableMapper.class},
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskStatusMapper {
    TaskStatusDTO toDTO(TaskStatus status);

    TaskStatus toEntity(TaskStatusCreateDTO dto);

    void update(TaskStatusPathDTO dto, @MappingTarget TaskStatus status);
}
