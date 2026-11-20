package hexlet.code.app.service;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskPatchDTO;
import hexlet.code.app.exception.ResourceAlreadyExistException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    public List<TaskDTO> index() {
        return taskRepository.findAll().stream().map(taskMapper::toDTO).toList();
    }

    public TaskDTO findById(Long id) {
        return taskRepository.findById(id).map(taskMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    public TaskDTO create(TaskCreateDTO data) {
        if (taskRepository.findByName(data.getName()).isPresent()) {
            throw new ResourceAlreadyExistException("Task with name \"" + data.getName() + "\" already exists");
        }
        var task = taskMapper.toEntity(data);
        return taskMapper.toDTO(taskRepository.save(task));
    }

    public TaskDTO update(TaskPatchDTO data, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        if (data.getName() != null && data.getName().isPresent() && data.getName().get() != null) {
            var newName = data.getName().get();
            var existingName = taskRepository.findByName(newName);
            if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
                throw new ResourceAlreadyExistException("Task name \"" + newName + "\" already exists");
            }
        }

        if (data.getIndex() != null && data.getIndex().isPresent() && data.getIndex().get() != null) {
            var newIndex = data.getIndex().get();
            var existingIndex = taskRepository.findByIndex(newIndex);
            if (existingIndex.isPresent() && !existingIndex.get().getId().equals(id)) {
                throw new ResourceAlreadyExistException("Task index \"" + newIndex + "\" already exists");
            }
        }

        taskMapper.update(data, task);
        return taskMapper.toDTO(taskRepository.save(task));
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task with id " + id + " not found");
        }
        taskRepository.deleteById(id);
    }
}
