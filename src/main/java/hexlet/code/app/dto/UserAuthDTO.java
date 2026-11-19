package hexlet.code.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDTO {
    @JsonAlias("username")
    private String email;
    private String password;
}
