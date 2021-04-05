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
import ru.sberinsur.insure.integrations.dto.common.doc.DocIgnore;
import ru.sberinsur.insure.integrations.dto.template.ReponseTemplateDto;
import ru.sberinsur.insure.integrations.dto.template.RequestTemplateDto;
import ru.sberinsur.insure.integrations.exception.InsureException;
import ru.sberinsur.insure.util.MapperUtil;


import java.util.Map;


/**
 * Group listener example based on InsureListener
 */
@Service
@KafkaListener(containerFactory = "groupKafkaListenerContainerFactory",
        topics = {"${kafka.groupConsumer.topic}"})
@Slf4j
@RequiredArgsConstructor
public class GroupListener extends InsureListener {

    private final KafkaHelper kafkaHelper;
    private final KafkaConfig kafkaConfig;

    @KafkaHandler
    @SendTo("!{source.headers['kafka_replyTopic']}")
    public Message<ReponseTemplateDto> getCustomer(RequestTemplateDto requestTemplateDto, @Headers Map<String, Object> headers, Acknowledgment ack) {
        //Audit subsystem call

        try {
            log.info("[getCustomer] Received request");
            log.info("[getCustomer] Request headers: {}", headers);
            log.trace("[getCustomer] Received group topic {}", MapperUtil.toJsonString(requestTemplateDto));
            if (null != headers && !headers.isEmpty()) {
                this.kafkaHelper.defaultPrintNotNullKafkaHeaders(headers);
            }
            ack.acknowledge();

            return genericMessageResult(new ReponseTemplateDto("customerResponse"),headers);
        } catch (Exception e) {
            throw new InsureException(e.getMessage(), e, TemplateErrors.TEMPLATE_ERRORS);
        }
    }


    @KafkaHandler
    @DocIgnore
    public void processCustomersResponse(ReponseTemplateDto reponseTemplateDto, @Headers Map<String, Object> headers, Acknowledgment ack) {

        log.info("Received ReponseCustomersDto specific topic {}", reponseTemplateDto);
        log.info("Headers: " + headers);
        log.info(reponseTemplateDto.getResponse());
        ack.acknowledge();

    }



    private <T extends ru.sberinsur.insure.integrations.commons.Message> Message genericMessageResult(T message, Map<String, Object> headers) {
        return new GenericMessage(message,
                this.kafkaHelper.correlateHeaders(headers, this.defaultKafkaConfig.getSpecificConsumer().getGroupId()));
    }
}
