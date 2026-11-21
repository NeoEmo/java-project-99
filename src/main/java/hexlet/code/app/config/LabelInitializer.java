package hexlet.code.app.config;

import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LabelInitializer implements CommandLineRunner {

    @Autowired
    private LabelRepository labelRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (labelRepository.count() > 0) {
            return;
        }
        var feature = new Label();
        feature.setName("feature");

        var bug = new Label();
        bug.setName("bug");

        labelRepository.saveAll(List.of(feature, bug));

        System.out.println("Дефолтные метки были успешно добавлены");
    }
}
