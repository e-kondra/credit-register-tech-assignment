package com.example.backend.service;

import com.example.backend.dto.CreditBanEvent;
import com.example.backend.entity.CreditExtract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.creditBan}")
    private String creditBanTopic;

    public void sendCreditBanEvent(CreditExtract extract) {
        try {
            CreditBanEvent event = CreditBanEvent.builder()
                    .ssn(extract.getSsn())
                    .extractReference(extract.getExtractReference())
                    .reason(extract.getBanReason())
                    .fetchDate(extract.getFetchDate())
                    .eventTime(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(creditBanTopic, extract.getSsn(), event);
            log.info("Published credit ban event for SSN: {}", extract.getSsn());
        } catch (Exception e) {
            log.error("Failed to send credit ban event to Kafka for SSN: {}", extract.getSsn(), e);
        }
    }
}
