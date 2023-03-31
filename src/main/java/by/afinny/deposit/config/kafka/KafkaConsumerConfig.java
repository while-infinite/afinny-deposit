package by.afinny.deposit.config.kafka;

import by.afinny.deposit.config.kafka.properties.KafkaConfigProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaConfigProperties.class)
public class KafkaConsumerConfig {

    private final KafkaConfigProperties config;
    private KafkaProperties kafkaProperties;
    private String BOOTSTRAP_SERVERS;

    @PostConstruct
    private void createKafkaProperties() {
        kafkaProperties = config.getKafkaProperties();
        BOOTSTRAP_SERVERS = config.getBootstrapServers();
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> stubConsumerFactoryForUpdateCardStatus() {
        Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties();
        kafkaConsumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "by.afinny.deposit.dto.kafka.CardEvent");
        return new DefaultKafkaConsumerFactory<>(kafkaConsumerProperties);
    }

    @Bean(name = "listenerFactoryForCardStatus")
    public ConcurrentKafkaListenerContainerFactory<String, Object> factoryForCardStatus(ConsumerFactory<String, Object> stubConsumerFactoryForUpdateCardStatus) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stubConsumerFactoryForUpdateCardStatus);
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> stubConsumerFactoryForNewDeposit() {
        Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties();
        kafkaConsumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "by.afinny.deposit.dto.RequestNewDepositDto");
        return new DefaultKafkaConsumerFactory<>(kafkaConsumerProperties);
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> consumerFactoryForWithdrawDeposit() {
        Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties();
        kafkaConsumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent");
        return new DefaultKafkaConsumerFactory<>(kafkaConsumerProperties);
    }

    @Bean(name = "stubListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> stubFactory(ConsumerFactory<String, Object> stubConsumerFactoryForNewDeposit) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stubConsumerFactoryForNewDeposit);
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> consumerFactoryForNewDeposit() {
        Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties();
        kafkaConsumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "by.afinny.deposit.entity.Agreement");
        return new DefaultKafkaConsumerFactory<>(kafkaConsumerProperties);
    }

    @Bean(name = "listenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> factory(ConsumerFactory<String, Object> consumerFactoryForNewDeposit) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryForNewDeposit);
        return factory;
    }

    @Bean(name = "listenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> factoryForWithdrawDeposit(ConsumerFactory<String, Object> consumerFactoryForWithdrawDeposit) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryForWithdrawDeposit);
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> stubConsumerFactoryNewCard() {
        Map<String, Object> properties = getKafkaConsumerProperties();
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "by.afinny.deposit.dto.kafka.ProducerNewCardEvent");
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean(name = "stubKafkaListenerNewCard")
    public ConcurrentKafkaListenerContainerFactory<String, Object> stubFactoryNewCard() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stubConsumerFactoryNewCard());

        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> consumerFactoryNewCard() {
        Map<String, Object> properties = getKafkaConsumerProperties();
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "by.afinny.deposit.dto.kafka.ConsumerNewCardEvent");
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean(name = "kafkaListenerNewCard")
    public ConcurrentKafkaListenerContainerFactory<String, Object> factoryNewCard() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryNewCard());

        return factory;
    }

    private Map<String, Object> getKafkaConsumerProperties() {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProperties.get("key.deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProperties.get("value.deserializer"));
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);

        return props;
    }
}
