package hexlet.code.app.repository;

import hexlet.code.app.model.Task;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @EntityGraph(attributePaths = {"labels", "taskStatus"})
    Optional<Task> findByName(String name);

    Optional<Task> findByIndex(Integer index);
    boolean existsByAssigneeId(Long assigneeId);
    boolean existsByTaskStatusId(Long statusId);

    /**
     * @param spec     must not be {@literal null}.
     * @param pageable must not be {@literal null}.
     */
    @Override
    @EntityGraph(attributePaths = {"assignee", "taskStatus"})
    Page<Task> findAll(@NonNull Specification<Task> spec, @NonNull Pageable pageable);

    /**
     * @param id must not be {@literal null}.
     */
    @Override
    @EntityGraph(attributePaths = {"assignee", "taskStatus", "labels"})
    Optional<Task> findById(@NonNull Long id);
}
