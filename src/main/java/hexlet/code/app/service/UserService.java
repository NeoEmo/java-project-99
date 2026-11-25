package hexlet.code.app.service;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserPatchDTO;
import hexlet.code.app.exception.ResourceAlreadyExistException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.exception.EntityAlreadyAssignedToTaskException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Object, т.к. нету UserParamsDTO

@Service
@Transactional
public class UserService implements CRUDService<UserDTO, UserCreateDTO, UserPatchDTO, Object> {
	private final UserRepository userRepository;
	private final TaskRepository taskRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserService(UserRepository userRepository,
					   TaskRepository taskRepository,
					   UserMapper userMapper,
					   PasswordEncoder passwordEncoder) {

		this.userRepository = userRepository;
		this.taskRepository = taskRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<UserDTO> index(int page, int size) {
		var safePage = Math.max(0, page - 1);
		var pageable = PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return userRepository.findAll(pageable).map(userMapper::toDTO).toList();
	}

	@Override
	public UserDTO create(UserCreateDTO data) {
		// Пока сделана проверка на уникальность только у почты, но если нужно,
		// добавлены методы по поиску имени и фамилии
		/*
		if (userRepository.findByFirstName(data.getFirstName()).isPresent()) {
			throw new ResourceAlreadyExistException("User firstName \""
					+ data.getFirstName() + "\" already exists");
		}
		if (userRepository.findByLastName(data.getLastName()).isPresent()) {
			throw new ResourceAlreadyExistException("User lastName \""
					+ data.getLastName() + "\" already exists");
		}
		*/

		if (userRepository.findByEmail(data.getEmail()).isPresent()) {
			throw new ResourceAlreadyExistException("User email \""
					+ data.getEmail() + "\" already exists");
		}


		data.setPassword(passwordEncoder.encode(data.getPassword()));
		var user = userMapper.toEntity(data);
		return userMapper.toDTO(userRepository.save(user));
	}

	@Override
	public UserDTO findById(Long id) {
		return userRepository.findById(id).map(userMapper::toDTO)
				.orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found!"));
	}

	public UserDTO update(UserPatchDTO data, Long id) {

		var user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found!"));

		if (data.getEmail() != null && data.getEmail().isPresent() && data.getEmail().get() != null) {
			var newEmail = data.getEmail().get();
			var existingUser = userRepository.findByEmail(newEmail);

			if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
				throw new ResourceAlreadyExistException("User email \""
						+ newEmail + "\" already exists");
			}
		}

		userMapper.update(data, user);

		if (data.getPassword() != null && data.getPassword().isPresent() && data.getPassword().get() != null) {
			var encodedPassword = passwordEncoder.encode(user.getPasswordDigest());
			user.setPasswordDigest(encodedPassword);
		}

		userRepository.save(user);
		return userMapper.toDTO(user);
	}

	@Override
	public void delete(Long id) {
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User with id " + id + " not found!");
		}

		if (taskRepository.existsByAssigneeId(id)) {
			throw new EntityAlreadyAssignedToTaskException(
					"User with id " + id + " already assigned to task!");
		}

		userRepository.deleteById(id);
	}
}
