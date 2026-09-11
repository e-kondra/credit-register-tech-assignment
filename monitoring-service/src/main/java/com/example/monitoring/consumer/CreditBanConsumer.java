package com.example.monitoring.consumer;

import com.example.monitoring.dto.CreditBanEvent;
import com.example.monitoring.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditBanConsumer {
    private final EmailService emailService;
    private final ObjectMapper objectMapper;


    @KafkaListener(
            topics="${kafka.topic.creditBan}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {

        log.info("Received credit ban event: {}", message);

        try {
            CreditBanEvent event = objectMapper.readValue(message, CreditBanEvent.class);
            log.info("Deserialized event: {}", event);
            emailService.sendCreditBanNotification(event);
            log.info("Email sent successfully for SSN: {}", event.getSsn());
        } catch (Exception e) {
            log.error("Failed to send email for credit ban event: {}", message, e);
        }
    }

}
