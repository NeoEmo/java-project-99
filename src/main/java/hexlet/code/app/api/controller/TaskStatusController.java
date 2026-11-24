package hexlet.code.app.api.controller;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;

import hexlet.code.app.dto.taskStatus.TaskStatusPatchDTO;
import hexlet.code.app.service.TaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RequestMapping("/api/task_statuses")
@RestController
public class TaskStatusController {
    private final TaskStatusService taskStatusService;

    @Autowired
    public TaskStatusController(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    @GetMapping
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var list = taskStatusService.index();

        return ResponseEntity.ok().header("X-total-Count", String.valueOf(list.size())).body(list);
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

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<TaskStatusDTO> update(@PathVariable Long id, @RequestBody TaskStatusPatchDTO data) {
        return ResponseEntity.ok().body(taskStatusService.update(data, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> delete(@PathVariable Long id) {
        taskStatusService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
