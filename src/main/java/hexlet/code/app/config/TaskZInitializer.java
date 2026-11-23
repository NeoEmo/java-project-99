package hexlet.code.app.config;

import hexlet.code.app.model.Task;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TaskZInitializer implements CommandLineRunner {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (taskRepository.count() > 0) {
            return;
        }

        var task = new Task();
        task.setName("Task 1");
        task.setTaskStatus(taskStatusRepository.findByName("Draft").get());

        var task2 = new Task();
        task2.setName("Task 2");
        task2.setTaskStatus(taskStatusRepository.findByName("ToReview").get());

        var task3 = new Task();
        task3.setName("Task 3");
        task3.setTaskStatus(taskStatusRepository.findByName("ToBeFixed").get());

        var task4 = new Task();
        task4.setName("Task 4");
        task4.setTaskStatus(taskStatusRepository.findByName("ToPublish").get());

        var task5 = new Task();
        task5.setName("Task 5");
        task5.setTaskStatus(taskStatusRepository.findByName("Published").get());

        taskRepository.saveAll(List.of(task,task2,task3,task4,task5));

        System.out.println("Task initialized");
    }
}
