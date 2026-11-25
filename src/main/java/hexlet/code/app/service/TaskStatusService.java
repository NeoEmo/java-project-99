package hexlet.code.app.service;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusPatchDTO;
import hexlet.code.app.exception.EntityAlreadyAssignedToTaskException;
import hexlet.code.app.exception.ResourceAlreadyExistException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Object, т.к. нету TaskStatusParamsDTO

@Service
@Transactional
public class TaskStatusService implements CRUDService<TaskStatusDTO, TaskStatusCreateDTO, TaskStatusPatchDTO, Object> {
    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusMapper taskStatusMapper;

    @Autowired
    public TaskStatusService(TaskStatusRepository taskStatusRepository,
                             TaskRepository taskRepository,
                             TaskStatusMapper taskStatusMapper) {

        this.taskStatusRepository = taskStatusRepository;
        this.taskRepository = taskRepository;
        this.taskStatusMapper = taskStatusMapper;
    }

    @Override
    public List<TaskStatusDTO> index() {
        return taskStatusRepository.findAll().stream().map(taskStatusMapper::toDTO).toList();
    }

    @Override
    public TaskStatusDTO findById(Long id) {
        return taskStatusRepository.findById(id).map(taskStatusMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id: " + id + " not found!"));
    }

    @Override
    public TaskStatusDTO create(TaskStatusCreateDTO data) {
        if (taskStatusRepository.findBySlug(data.getSlug()).isPresent()) {
            throw new ResourceAlreadyExistException("Task status with slug: " + data.getSlug() + " already exist!");
        }
        var taskStatus = taskStatusMapper.toEntity(data);
        return taskStatusMapper.toDTO(taskStatusRepository.save(taskStatus));
    }

    @Override
    public TaskStatusDTO update(TaskStatusPatchDTO data, Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id: " + id + " not found!"));

        if (data.getSlug() != null && data.getSlug().isPresent() && data.getSlug().get() != null) {
            var newSlug = data.getSlug().get();
            var existingSlug = taskStatusRepository.findBySlug(newSlug);
            if (existingSlug.isPresent() && !existingSlug.get().getId().equals(id)) {
                throw new ResourceAlreadyExistException("Task status slug \"" + newSlug + "\" already exist!");
            }
        }

        if (data.getName() != null && data.getName().isPresent() && data.getName().get() != null) {
            var newName = data.getName().get();
            var existingName = taskStatusRepository.findByName(newName);
            if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
                throw new ResourceAlreadyExistException("Task status name \"" + newName + "\" already exist!");
            }
        }

        taskStatusMapper.update(data, taskStatus);
        return taskStatusMapper.toDTO(taskStatusRepository.save(taskStatus));
    }

    @Override
    public void delete(Long id) {
        if (!taskStatusRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task status with id: " + id + " not found!");
        }

        if (taskRepository.existsByTaskStatusId(id)) {
            throw new EntityAlreadyAssignedToTaskException("Task status with id: " + id + " already assigned to task!");
        }

        taskStatusRepository.deleteById(id);
    }
}
