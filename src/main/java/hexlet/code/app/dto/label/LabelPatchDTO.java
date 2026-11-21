package hexlet.code.app.dto.label;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class LabelPatchDTO {

    private JsonNullable<String> name;
}
