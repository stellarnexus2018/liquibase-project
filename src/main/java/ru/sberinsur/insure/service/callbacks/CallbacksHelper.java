package ru.sberinsur.insure.service.callbacks;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.sberinsur.insure.integrations.commons.Headers;
import ru.sberinsur.insure.integrations.commons.KafkaHeaderAccessor;
import ru.sberinsur.insure.integrations.commons.Message;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.Map;
import java.util.UUID;
import ru.sberinsur.insure.integrations.commons.KafkaHelper;

import static ru.sberinsur.insure.integrations.commons.Headers.INSURE_CHAIN_RQ_ID;
import static ru.sberinsur.insure.integrations.commons.KafkaHelper.bytesToUUID;

@Component
@RequiredArgsConstructor
public class CallbacksHelper {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    /**
     * Send chain result dto. Equal outDtoType parameter from @ChainHandler
     * <p>
     * For example: @ChainHandler(outDtoType = SearchApplicationByFiltersResult.class)
     *
     * @param headers
     * @param message
     */
    public void sendChainResult(Map<String, Object> headers, Message message) {
        KafkaHeaderAccessor kafkaHeaderAccessor = KafkaHeaderAccessor.ofMap(headers);
        ProducerRecord<String, Message> producerRecord = new ProducerRecord<>(kafkaHeaderAccessor.topic(), null, UUID.randomUUID().toString(), message, kafkaHeaderAccessor.toKafkaHeaders());
        this.kafkaTemplate.send(producerRecord);
    }

    /**
     * Check and fetch INSURE_CHAIN_RQ_ID
     *
     * @param rootHeaders
     * @return
     */
    public UUID getChainRq(Map<String, Object> rootHeaders) {
        if (null == rootHeaders.get(INSURE_CHAIN_RQ_ID.name()))
            throw new IllegalArgumentException(String.format("Empty %s header", Headers.INSURE_CHAIN_RQ_ID.name()));
        return bytesToUUID((byte[]) rootHeaders.get(INSURE_CHAIN_RQ_ID.name()));
    }

}
