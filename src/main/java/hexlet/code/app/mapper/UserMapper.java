package hexlet.code.app.mapper;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserPatchDTO;
import hexlet.code.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

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
