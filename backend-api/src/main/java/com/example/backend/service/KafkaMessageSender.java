package com.example.backend.service;

import com.example.backend.dto.CreditBanEvent;
import com.example.backend.entity.CreditExtract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.creditBan}")
    private String creditBanTopic;

    public void sendCreditBanEvent(CreditExtract extract) {
        CreditBanEvent event = CreditBanEvent.builder()
                    .ssn(extract.getSsn())
                    .extractReference(extract.getExtractReference())
                    .reason(extract.getBanReason())
                    .fetchDate(extract.getFetchDate())
                    .eventTime(LocalDateTime.now())
                    .build();

        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(creditBanTopic, event.getSsn(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Sent credit ban event for SSN: {} to partition {} offset {}",
                            event.getSsn(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send credit ban event for SSN: {}",
                            event.getSsn(), exception);
                }
            });

            // Block for 5 sec to guarantee delivery
            future.get(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Kafka send failed for SSN: {}. Event will be lost. " +
                    "Consider implementing outbox pattern for prod.",
                    extract.getSsn(), e);
        }
    }
}
