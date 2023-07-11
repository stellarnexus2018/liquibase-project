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
import ru.sberinsur.insure.exception.TemplateErrors;
import ru.sberinsur.insure.integrations.commons.InsureListener;
import ru.sberinsur.insure.integrations.dto.bpm.common.ErrorDto;
import ru.sberinsur.insure.integrations.dto.bpm.externalTask.CompleteExternalTaskDto;
import ru.sberinsur.insure.integrations.dto.bpm.externalTask.ExecuteExternalTaskDto;
import ru.sberinsur.insure.integrations.dto.common.doc.DocIgnore;
import ru.sberinsur.insure.integrations.dto.template.ReponseTemplateDto;
import ru.sberinsur.insure.integrations.dto.template.RequestTemplateDto;
import ru.sberinsur.insure.integrations.exception.InsureException;
import ru.sberinsur.insure.integrations.exception.SystemErrorCode;
import ru.sberinsur.insure.util.MapperUtil;

import java.util.Arrays;
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

            return genericMessageResult(new ReponseTemplateDto("customerResponse"), headers);
        } catch (Exception e) {
            throw new InsureException(e.getMessage(), e, TemplateErrors.TEMPLATE_ERRORS);
        }
    }

    @KafkaHandler
    @SendTo("!{source.headers['kafka_replyTopic']}")
    public Message<CompleteExternalTaskDto> bpmRequest(ExecuteExternalTaskDto request, @Headers Map<String, Object> headers, Acknowledgment ack) {
        log.info("[bpmRequest] Received request");
        log.info("[bpmRequest] Request headers: {}", headers);
        try {
            log.trace("[bpmRequest] Received group topic {}", MapperUtil.toJsonString(request));
            if (null != headers && !headers.isEmpty()) {
                this.kafkaHelper.defaultPrintNotNullKafkaHeaders(headers);
            }
            var completeExternalTaskDto = processingBpmRequest(request, headers);
            ack.acknowledge();
            var outgoingHeaders = kafkaHelper.correlateHeaders(headers,
                    defaultKafkaConfig.getSpecificConsumer().getGroupId());
            return new GenericMessage<>(completeExternalTaskDto, outgoingHeaders);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new InsureException(e.getMessage(), e, SystemErrorCode.INTERNAL_ERROR);
        }
    }

    private CompleteExternalTaskDto processingBpmRequest(ExecuteExternalTaskDto request, Map<String, Object> headers) {
        try {
            // business logic call here
            Map<String, Object> returnVariables = Map.of();

            return new CompleteExternalTaskDto(request.getTask(), returnVariables, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String businessError = (e instanceof InsureException) ?
                    ((InsureException) e).getInsureErrorCode().getErrorDescription() : null;
            return new CompleteExternalTaskDto(request.getTask(), Map.of(), new ErrorDto(e.getMessage(), Arrays.toString(e.getStackTrace()), businessError));
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


    private <T extends ru.sberinsur.insure.integrations.commons.Message> Message<T> genericMessageResult(T message, Map<String, Object> headers) {
        return new GenericMessage<>(message,
                this.kafkaHelper.correlateHeaders(headers, this.defaultKafkaConfig.getSpecificConsumer().getGroupId()));
    }
}
