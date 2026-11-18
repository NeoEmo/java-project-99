package hexlet.code.app.api.controller;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserPatchDTO;
import hexlet.code.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping
	public ResponseEntity<Page<UserDTO>> index(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(userService.index(page, size));
	}

	@PostMapping
	public ResponseEntity<UserDTO> create(@RequestBody UserCreateDTO data) {
		var dto = userService.create(data);
		var location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(dto.getId())
				.toUri();

		return ResponseEntity.created(location).body(dto);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserPatchDTO data) {
		return ResponseEntity.ok().body(userService.update(data, id));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> show(@PathVariable Long id) {
		return ResponseEntity.ok().body(userService.findById(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
