package ru.sberinsur.insure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.sberinsur.insure.integrations.commons.Message;
import ru.sberinsur.insure.integrations.commons.chain.Waiter;
import ru.sberinsur.insure.integrations.commons.marshall.InsureKafkaDeserializer;
import ru.sberinsur.insure.integrations.commons.marshall.InsureKafkaSerializer;
import ru.sberinsur.insure.integrations.exception.InsureKafkaErrorHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {

    private KafkaConfig kafkaConfig;
    private Waiter waiter;

    @Autowired
    public KafkaConfiguration(KafkaConfig kafkaConfig, Waiter waiter) {
        this.kafkaConfig = kafkaConfig;
        this.waiter = waiter;
    }

    @Bean
    public KafkaTemplate<String, Message> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, Message> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfig.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, InsureKafkaSerializer.class);
        if (!this.kafkaConfig.getProperties().isEmpty()) {
            props.putAll(this.kafkaConfig.getProperties());
        }
        //props.putAll(this.kafkaConfig.getProperties());  uncomment if need properties (e.g ssl configuration)
        return props;
    }

    @Bean
    public Map<String, Object> groupConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, InsureKafkaDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.kafkaConfig.getGroupConsumer().getGroupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        if (!this.kafkaConfig.getProperties().isEmpty()) {
            props.putAll(this.kafkaConfig.getProperties());
        }
        //props.putAll(this.kafkaConfig.getProperties());  uncomment if need properties (e.g ssl configuration)
        return props;
    }

    @Bean
    public Map<String, Object> specificConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, InsureKafkaDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.kafkaConfig.getSpecificConsumer().getGroupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        if (!this.kafkaConfig.getProperties().isEmpty()) {
            props.putAll(this.kafkaConfig.getProperties());
        }
        //props.putAll(this.kafkaConfig.getProperties());  uncomment if need properties (e.g ssl configuration)
        return props;
    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Message>> specificKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Message> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(specificConsumerFactory());
        factory.setConcurrency(this.kafkaConfig.getSpecificConsumer().getConcurrency());
        factory.getContainerProperties().setPollTimeout(this.kafkaConfig.getSpecificConsumer().getPollTimeout());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setReplyTemplate(kafkaTemplate());
        factory.setErrorHandler(new InsureKafkaErrorHandler(kafkaTemplate(),
                this.kafkaConfig.getErrorTopic(), this.kafkaConfig.getSpecificConsumer().getGroupId(), waiter));
        return factory;
    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Message>> groupKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Message> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(groupConsumerFactory());
        factory.setConcurrency(this.kafkaConfig.getGroupConsumer().getConcurrency());
        factory.getContainerProperties().setPollTimeout(this.kafkaConfig.getGroupConsumer().getPollTimeout());
        factory.getContainerProperties().setAckMode((ContainerProperties.AckMode.MANUAL));
        factory.setReplyTemplate(kafkaTemplate());
        factory.setErrorHandler(new InsureKafkaErrorHandler(kafkaTemplate(),
                this.kafkaConfig.getErrorTopic(), this.kafkaConfig.getSpecificConsumer().getGroupId(), waiter));
        return factory;
    }


    private ConsumerFactory<String, Message> groupConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(groupConsumerConfig());
    }

    private ConsumerFactory<String, Message> specificConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(specificConsumerConfig());
    }


}
