package com.chinmay.GPSService1.listener; // Or your listener package

import com.chinmay.GPSService1.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message; // Import this to get headers
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class GpsDeadLetterQueueListener {

    @RabbitListener(queues = RabbitMQConfig.GPS_DLQ_NAME)
    public void handleDeadLetter(Message failedMessage) { // Receive the raw message to inspect headers
        String messageBody = new String(failedMessage.getBody());
        log.warn("DLQ Listener: Received dead-lettered message. Payload: {}", messageBody);

        // Log message properties, especially 'x-death' which contains reasons for dead-lettering
        log.warn("DLQ Message Properties: {}", failedMessage.getMessageProperties());
        Map<String, Object> headers = failedMessage.getMessageProperties().getHeaders();
        if (headers.containsKey("x-death")) {
            log.warn("DLQ x-death header: {}", headers.get("x-death"));
            // The x-death header is an array of objects, each describing a dead-lettering event.
            // It can tell you the original queue, exchange, reason, and count.
        }

        // TODO: Implement your strategy for these failed messages:
        // 1. Log for manual inspection/debugging. (Already doing this)
        // 2. Send an alert (e.g., email, Slack).
        // 3. Store them in a separate "failed messages" database table for later analysis.
    }
}
