package hexlet.code.app.config;

import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskStatusInitializer implements CommandLineRunner {

    @Autowired
    private TaskStatusRepository taskStatusRepository;


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (taskStatusRepository.count() > 0) {
            return;
        }
        var draft = new TaskStatus();
        draft.setName("Draft");
        draft.setSlug("draft");

        var toReview = new TaskStatus();
        toReview.setName("Review");
        toReview.setSlug("to_review");

        var toBeFixed = new TaskStatus();
        toBeFixed.setName("To be fixed");
        toBeFixed.setSlug("to_be_fixed");

        var toPublish = new TaskStatus();
        toPublish.setName("To publish");
        toPublish.setSlug("to_publish");

        var published = new TaskStatus();
        published.setName("Published");
        published.setSlug("published");

        taskStatusRepository.saveAll(List.of(draft, toReview, toBeFixed, toPublish, published));

        System.out.println("Дефолтные статусы задач успешно добавлены в репозиторий!");
    }
}
