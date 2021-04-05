package ru.sberinsur.insure.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.sberinsur.insure.integrations.config.DefaultKafkaConfig;

/**
 * Kafka properties from config file (template.yml) based on DefaultKafkaConfig
 */
@Configuration
@ConfigurationProperties(prefix = "kafka")
@Data
@EqualsAndHashCode(callSuper = true)
public class KafkaConfig extends DefaultKafkaConfig {

    private Producers producers;

    public KafkaConfig() {
        super();
    }

    @Data
    public static class Producer {

        private String groupTopic;
        private String specificTopic;
    }

    @Data
    public static class Producers {
        private Producer template;
        private Producer bpm;
    }


}
