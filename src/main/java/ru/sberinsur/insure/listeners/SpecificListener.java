package ru.sberinsur.insure.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import ru.sberinsur.insure.config.KafkaConfig;
import ru.sberinsur.insure.exception.TemplateErrors;
import ru.sberinsur.insure.integrations.commons.KafkaHelper;
import ru.sberinsur.insure.integrations.commons.InsureListener;
import ru.sberinsur.insure.integrations.dto.template.ReponseTemplateDto;
import ru.sberinsur.insure.integrations.dto.template.RequestTemplateDto;
import ru.sberinsur.insure.integrations.exception.InsureException;

import java.util.Map;


/**
 * Specific listener example based on InsureListener
 */
@Service
@KafkaListener(containerFactory = "specificKafkaListenerContainerFactory",
        topics = {"${kafka.specificConsumer.topic}"})
@Slf4j
@RequiredArgsConstructor
public class SpecificListener extends InsureListener {

    private final KafkaConfig kafkaConfig;
    private final KafkaHelper kafkaHelper;


    @KafkaHandler
    @SendTo("!{source.headers['kafka_replyTopic']}")
    public Message<ReponseTemplateDto> getCustomer(RequestTemplateDto requestTemplateDto, @Headers Map<String, Object> headers, Acknowledgment ack) {

        try {
            log.info("Received specific topic {}", requestTemplateDto);
            log.info("Headers: " + headers);
            ack.acknowledge();
            return new GenericMessage<>(new ReponseTemplateDto("customerResponse"), this.kafkaHelper.correlateHeaders(headers, this.kafkaConfig.getSpecificConsumer().getGroupId()));
        } catch (Exception e) {
            throw new InsureException(e.getMessage(), e, TemplateErrors.TEMPLATE_ERRORS);
        }


    }


    @KafkaHandler
    public void processCustomersResponse(ReponseTemplateDto reponseTemplateDto, @Headers Map<String, Object> headers, Acknowledgment ack) {

        log.info("Received ReponseCustomersDto specific topic {}", reponseTemplateDto);
        log.info("Headers: " + headers);
        ack.acknowledge();

    }


}
