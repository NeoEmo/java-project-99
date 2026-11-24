package hexlet.code.app.dto.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDTO {
    @JsonAlias("username")
    @Email
    private String email;
    private String password;
}
