package hexlet.code.app.specification;

import hexlet.code.app.dto.task.TaskParamsDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {
    public Specification<Task> build(TaskParamsDTO params) {
        return withTitleCont(params.getTitleCont())
                .and(withAssigneeId(params.getAssigneeId()))
                .and(withStatus(params.getStatus()))
                .and(withLabelId(params.getLabelId()));
    }

    private Specification<Task> withTitleCont(String titleCont) {
        return (root, query, cb) ->
                titleCont == null
                        ? cb.conjunction()
                        : cb.equal(root.get("name"), titleCont);
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) ->
                assigneeId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, cb) ->
                status == null
                        ? cb.conjunction()
                        : cb.equal(root.get("taskStatus"), status);
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) -> {
            if (labelId == null) return cb.conjunction();

            Join<Task, Label> join = root.join("labels");
            query.distinct(true);

            return cb.equal(join.get("id"), labelId);
        };
    }
}
