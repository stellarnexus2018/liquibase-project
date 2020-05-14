package ru.sberinsur.insure.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberinsur.insure.config.KafkaConfig;
import ru.sberinsur.insure.exception.TemplateErrors;
import ru.sberinsur.insure.integrations.commons.Headers;
import ru.sberinsur.insure.integrations.commons.Message;
import ru.sberinsur.insure.integrations.dto.template.RequestTemplateDto;
import ru.sberinsur.insure.integrations.exception.InsureException;
import static ru.sberinsur.insure.integrations.commons.KafkaHelper.intToBytes;
import static ru.sberinsur.insure.integrations.commons.KafkaHelper.uuidToBytes;
import java.util.UUID;

@Slf4j
@Component
public class TemplateService {
    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaTemplate<String, Message> kafkaTemplate;

    @Scheduled(fixedRate = 10000)
    public void test(){
        try {
            //Генерируем индентификатор сообщения
            UUID messageId = UUID.randomUUID();
            //Устанавливаем вызываемый сервис
            String serviceName = "template";
            //Заполняем вызываемый метод сервиса
            String methodName = "getCustomer";

            //Заполняем Response DTO сервиса (описывается в int-commons - секция DTO)
            RequestTemplateDto request = new RequestTemplateDto();
            request.setApplicationId("Test!");

            //Мапим Producer - устанавливается в конфигурации сервиса
            KafkaConfig.Producer serviceProducer = this.kafkaConfig.getProducerByString(serviceName);
            //Создаем сообщение
            ProducerRecord<String, Message> producerRecord = new ProducerRecord<>(serviceProducer.getGroupTopic(), messageId.toString(), request);
            //Устанавливаем заголовки
            producerRecord.headers().add(Headers.INSURE_SOURCE_INSTANCE_ID.name(), this.kafkaConfig.getSpecificConsumer().getGroupId().getBytes());
            producerRecord.headers().add(Headers.INSURE_MESSAGE_ID.name(), uuidToBytes(messageId));
            producerRecord.headers().add(Headers.INSURE_VERSION.name(), intToBytes(1));
            producerRecord.headers().add(KafkaHeaders.REPLY_TOPIC, this.kafkaConfig.getGroupConsumer().getTopic().getBytes());
            //Отправляем сообщение
            this.kafkaTemplate.send(producerRecord);
        } catch (Exception e) {
            throw new InsureException(e.getMessage(), e, TemplateErrors.TEMPLATE_ERRORS);
        }
    }
}
