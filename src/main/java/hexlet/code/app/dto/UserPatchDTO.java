package hexlet.code.app.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserPatchDTO {
	private JsonNullable<String> firstName;
	private JsonNullable<String> lastName;
	private JsonNullable<String> password;

	@Email
	private JsonNullable<String> email;
}
