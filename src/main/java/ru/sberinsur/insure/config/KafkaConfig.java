package ru.sberinsur.insure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.sberinsur.insure.integrations.config.DefaultKafkaConfig;

import java.util.HashMap;

/**
 * Kafka properties from config file (template.yml) based on DefaultKafkaConfig
 */
@Configuration
@ConfigurationProperties(prefix = "kafka")
@Data
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

    public Producer getProducerByString(String name){
        HashMap<String, Producer> producersMap = new HashMap<String, Producer>(){{
            put("bpm", producers.getBpm());
            put("template", producers.getTemplate());
        }};

        if (producersMap.containsKey(name.toLowerCase())){
            return producersMap.get(name.toLowerCase());
        }
        return null;
    }

}
