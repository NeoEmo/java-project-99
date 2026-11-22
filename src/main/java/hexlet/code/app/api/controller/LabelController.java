package hexlet.code.app.api.controller;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelPatchDTO;
import hexlet.code.app.service.LabelService;
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

@RequestMapping("/api/labels")
@RestController
public class LabelController {

    @Autowired
    private LabelService labelService;

    @GetMapping
    public ResponseEntity<List<LabelDTO>> index() {
        var list = labelService.index();

        return ResponseEntity.ok().header("X-total-Count", String.valueOf(list.size())).body(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabelDTO> show(@PathVariable Long id) {
        return ResponseEntity.ok().body(labelService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LabelDTO> create(@RequestBody LabelCreateDTO data) {
        var dto = labelService.create(data);
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(dto);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<LabelDTO> update(@PathVariable Long id, @RequestBody LabelPatchDTO data) {
        return ResponseEntity.ok().body(labelService.update(data, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LabelDTO> delete(@PathVariable Long id) {
        labelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
