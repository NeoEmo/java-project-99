package hexlet.code.app.api.controller;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;

import hexlet.code.app.dto.taskStatus.TaskStatusPathDTO;
import hexlet.code.app.service.TaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RequestMapping("/api/task_statuses")
@RestController
public class TaskStatusController {

    @Autowired
    private TaskStatusService taskStatusService;

    @GetMapping
    public List<TaskStatusDTO> getTaskStatuses() {
        return taskStatusService.index();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> show(@PathVariable Long id) {
        return ResponseEntity.ok().body(taskStatusService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskStatusDTO> create(@RequestBody TaskStatusCreateDTO data) {
        var dto = taskStatusService.create(data);
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> update(@PathVariable Long id, @RequestBody TaskStatusPathDTO data) {
        return ResponseEntity.ok().body(taskStatusService.update(data, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> delete(@PathVariable Long id) {
        taskStatusService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
