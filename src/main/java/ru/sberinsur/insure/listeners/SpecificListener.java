package ru.sberinsur.insure.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.validation.annotation.Validated;
import ru.sberinsur.insure.integrations.commons.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import ru.sberinsur.insure.config.KafkaConfig;
import ru.sberinsur.insure.exception.TemplateErrors;
import ru.sberinsur.insure.integrations.commons.KafkaHeaderAccessor;
import ru.sberinsur.insure.integrations.commons.KafkaHelper;
import ru.sberinsur.insure.integrations.commons.InsureListener;
import ru.sberinsur.insure.integrations.dto.template.ReponseTemplateDto;
import ru.sberinsur.insure.integrations.dto.template.RequestTemplateDto;
import ru.sberinsur.insure.integrations.exception.InsureErrorCode;
import ru.sberinsur.insure.integrations.exception.InsureException;


import java.util.Map;
import java.util.UUID;


/**
 * Specific listener example based on InsureListener
 */
@Service
@KafkaListener(containerFactory = "specificKafkaListenerContainerFactory",
        topics = {"${kafka.specificConsumer.topic}"})
@Slf4j
@Validated
@RequiredArgsConstructor
public class SpecificListener extends InsureListener {

    private final KafkaConfig kafkaConfig;
    private final KafkaHelper kafkaHelper;


    @KafkaHandler
    @SendTo("!{source.headers['kafka_replyTopic']}")
    public org.springframework.messaging.Message<ReponseTemplateDto> getCustomer(RequestTemplateDto requestTemplateDto, @Headers Map<String, Object> headers, Acknowledgment ack) {

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

    private void processChainResponseMessage(Map<String, Object> headers, ru.sberinsur.insure.integrations.commons.Message message, InsureErrorCode insureErrorCode, Acknowledgment ack) {

        KafkaHeaderAccessor kafkaHeaderAccessor = KafkaHeaderAccessor.ofMap(headers);
        if (null == kafkaHeaderAccessor.chainRequestId()) {
            throw new InsureException("INSURE_CHAIN_RQ_ID is null!");
        }

        try {
            if (kafkaHeaderAccessor.destinationInstanceId().equalsIgnoreCase(this.defaultKafkaConfig.getSpecificConsumer().getGroupId())) {
                log.info("Received message specific topic {}", message.toString());

                if (null != headers && !headers.isEmpty()) {
                    this.kafkaHelper.defaultPrintNotNullKafkaHeaders(headers);
                }

                UUID messageId = kafkaHeaderAccessor.correlationId();
                UUID rquid = kafkaHeaderAccessor.chainRequestId();
                log.debug("Rquid: {}; UUID: {}", rquid, messageId.toString());
                this.waiter.updateResponseHolder(rquid, messageId, message, headers);
            }
            ack.acknowledge();
        } catch (Exception e) {
            throw new InsureException(e.getMessage(), e, ((e instanceof InsureException) ? ((InsureException) e).getInsureErrorCode() : insureErrorCode));
        }
    }

}
