package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskPatchDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

@Mapper(uses = {ReferenceMapper.class, JsonNullableMapper.class},
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class TaskMapper {

    @Autowired
    protected TaskStatusRepository taskStatusRepository;

    @Autowired
    protected LabelRepository labelRepository;

    @Mapping(target = "labels", source = "labels", qualifiedByName = "labelToId")
    @Mapping(target = "taskStatusSlug", source = "taskStatus.slug")
    @Mapping(target = "assigneeId", source = "assignee.id")
    public abstract TaskDTO toDTO(Task task);

    @Mapping(target = "labels", source = "labels", qualifiedByName = "idToLabel")
    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "taskStatus", qualifiedByName = "slugToTaskStatus")
    @Mapping(target = "createdAt", ignore = true)
    public abstract Task toEntity(TaskCreateDTO taskDTO);

    @Mapping(target = "labels", source = "labels", qualifiedByName = "nullableIdToLabels")
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

    @Named("idToLabel")
    protected Label idToLabel(Long id) {
        if (id == null) {
            return null;
        }
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " +  id + " not found."));
    }

    @Named("labelToId")
    protected Long labelToId(Label label) {
        if (label == null) {
            return null;
        }
        return label.getId();
    }

    @Named("nullableIdToLabels")
    protected Set<Label> nullableIdToLabels(JsonNullable<Set<Long>> ids) {
        if (ids == null || !ids.isPresent() || ids.get() == null) {
            return Set.of();
        }
        Set<Long> setIds = ids.orElse(null);
        if (setIds == null) {
            return Set.of();
        }
        return new HashSet<>(labelRepository.findAllById(setIds));
    }
}
