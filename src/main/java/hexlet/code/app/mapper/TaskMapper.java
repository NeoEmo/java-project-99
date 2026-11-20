package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskPatchDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = {ReferenceMapper.class, JsonNullableMapper.class},
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class TaskMapper {

    @Autowired
    protected TaskStatusRepository taskStatusRepository;

    @Mapping(target = "taskStatusSlug", source = "taskStatus.slug")
    @Mapping(target = "assigneeId", source = "assignee.id")
    public abstract TaskDTO toDTO(Task task);

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "taskStatus", qualifiedByName = "slugToTaskStatus")
    @Mapping(target = "createdAt", ignore = true)
    public abstract Task toEntity(TaskCreateDTO taskDTO);

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "taskStatus", qualifiedByName = "slugToTaskStatus")
    public abstract void update(TaskPatchDTO dto, @MappingTarget Task task);

    @Named("slugToTaskStatus")
    protected TaskStatus slugToTaskStatus(String slug) {
        if (slug == null) {
            return null;
        }
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Slug \"" + slug + "\" not found."));
    }
}
