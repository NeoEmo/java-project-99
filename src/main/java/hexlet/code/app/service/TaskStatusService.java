package hexlet.code.app.service;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusPathDTO;
import hexlet.code.app.exception.ResourceAlreadyExistException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TaskStatusService {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> index() {
        return taskStatusRepository.findAll().stream().map(taskStatusMapper::toDTO).toList();
    }

    public TaskStatusDTO findById(Long id) {
        return taskStatusRepository.findById(id).map(taskStatusMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id: " + id + " not found!"));
    }

    public TaskStatusDTO create(TaskStatusCreateDTO data) {
        if (taskStatusRepository.findBySlug(data.getSlug()).isPresent()) {
            throw new ResourceAlreadyExistException("Task status with slug: " + data.getSlug() + " already exist!");
        }
        var taskStatus = taskStatusMapper.toEntity(data);
        return taskStatusMapper.toDTO(taskStatusRepository.save(taskStatus));
    }

    public TaskStatusDTO update(TaskStatusPathDTO data, Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id: " + id + " not found!"));

        var newSlug = data.getSlug().get();
        if (taskStatusRepository.findBySlug(newSlug).isPresent()) {
            throw new ResourceAlreadyExistException("Task status with slug: " + newSlug + " already exist!");
        }

        var newName = data.getName();
        if (taskStatusRepository.findBySlug(newSlug).isPresent()) {
            throw new ResourceAlreadyExistException("Task status with name: " + newName + " already exist!");
        }

        taskStatusMapper.update(data, taskStatus);
        return taskStatusMapper.toDTO(taskStatusRepository.save(taskStatus));
    }

    public void delete(Long id) {
        if (!taskStatusRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task status with id: " + id + " not found!");
        }
        taskStatusRepository.deleteById(id);
    }
}
