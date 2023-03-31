package by.afinny.deposit.integration.config.annotation;

import by.afinny.deposit.integration.config.initializer.KafkaContainerInitializer;
import by.afinny.deposit.integration.config.initializer.PostgresContainerInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "kafka.enabled=true"
)
@Testcontainers
@ActiveProfiles("integration")
@ContextConfiguration(initializers = {PostgresContainerInitializer.class, KafkaContainerInitializer.class})
public @interface TestWithKafkaContainer {
}
