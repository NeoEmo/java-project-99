package hexlet.code.app.mapper;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserPatchDTO;
import hexlet.code.app.model.User;
import org.mapstruct.*;

@Mapper(uses =  {ReferenceMapper.class, JsonNullableMapper.class},
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
	UserDTO toDTO(User user);

	@Mapping(target = "passwordDigest", source = "password")
	User toEntity(UserCreateDTO dto);

	@Mapping(target = "passwordDigest", source = "password")
	void update(UserPatchDTO dto, @MappingTarget User user);
}
