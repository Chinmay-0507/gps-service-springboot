package com.chinmay.GPSService1.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //When your Spring Boot application starts, it scans for classes marked with @Configuration. It then looks inside these classes for methods marked with @Bean
public class RabbitMQConfig {

    // Step 2: Define constants for names to ensure consistency and avoid "magic strings"
    // These names will be used by RabbitMQ server and your application code (producer/consumer).
    public static final String GPS_DATA_QUEUE_NAME = "gps-data-processing-queue";
    public static final String GPS_EXCHANGE_NAME = "gps-data-exchange";
    public static final String GPS_DATA_ROUTING_KEY = "gps.data.ingress"; // A routing key for new GPS data

    //Constants for a Dead Letter Queue (DLQ) - good for error handling later
    public static final String GPS_DLQ_NAME = "gps-data-dlq";
    public static final String GPS_DLX_NAME = "gps-data-dlx"; // Dead Letter Exchange
    public static final String GPS_DLQ_ROUTING_KEY = "gps.data.dead";

    // Step 3: Define the Queue as a Spring Bean
    @Bean // This annotation tells Spring that anf object will be created and should manage it (bean)
    public Queue gpsDataProcessingQueue() {
        return QueueBuilder.durable(GPS_DATA_QUEUE_NAME) // Your main queue
                .withArgument("x-dead-letter-exchange", GPS_DLX_NAME) // Is GPS_DLX_NAME correct?
                .withArgument("x-dead-letter-routing-key", GPS_DLQ_ROUTING_KEY) // Is GPS_DLQ_ROUTING_KEY correct?
                .build();
    }

    // Step 4: Define the Exchange as a Spring Bean
    @Bean
    public TopicExchange gpsDataTopicExchange() {
        // The constructor for TopicExchange is: new TopicExchange(String name)
        // You can also specify durability and auto-delete properties if needed:
        // new TopicExchange(String name, boolean durable, boolean autoDelete)
        // For exchanges, durability often means their metadata is preserved.
        return new TopicExchange(GPS_EXCHANGE_NAME, true, false); // Durable, not auto-delete
    }

    // Step 5: Define the Binding as a Spring Bean
    // This links your gpsDataProcessingQueue to your gpsDataTopicExchange.
    @Bean
    public Binding bindingGpsDataToExchange(Queue gpsDataProcessingQueue, TopicExchange gpsDataTopicExchange) {
        // Spring will automatically inject the 'gpsDataProcessingQueue' and 'gpsDataTopicExchange' beans
        // that were defined by the methods above.

        // BindingBuilder is a helper to create Binding objects.
        // - bind(queue): Start building a binding for the specified queue.
        // - to(exchange): Specify the exchange to bind the queue to.
        // - with(routingKey): Specify the routing key for this binding.
        //   Messages sent to 'gpsDataTopicExchange' with a routing key that matches 'GPS_DATA_ROUTING_KEY'
        //   (or a pattern that 'GPS_DATA_ROUTING_KEY' fits if the exchange was a topic and the binding key was a pattern)
        //   will be routed to 'gpsDataProcessingQueue'.
        //   For a TopicExchange, the binding key can use wildcards:
        //   Here, we're using an exact routing key for simplicity in the producer.
        return BindingBuilder.bind(gpsDataProcessingQueue)
                .to(gpsDataTopicExchange)
                .with(GPS_DATA_ROUTING_KEY);
    }

    // Step 6: Configure a Message Converter for JSON
    // This tells Spring AMQP how to convert message bodies.
    // If your producer sends Java objects directly (POJOs), and your consumer expects POJOs,
    // this converter will handle the serialization to JSON and deserialization from JSON.
    // In our current producer (GpsInputProcessor), we are manually converting the DTO to a JSON string.
    // However, this bean is very useful for the consumer side if it's annotated to receive an object type.
    @Bean
    public MessageConverter jsonMessageConverter() {
        // Jackson2JsonMessageConverter uses Jackson library for JSON processing.
        return new Jackson2JsonMessageConverter();
    }

    // Step 2a: Define the Dead Letter Exchange (DLX) as a Spring Bean

// A Direct Exchange is often suitable for a DLX, as you'll typically route
// all dead letters from a specific source to one DLQ.
    @Bean
    public DirectExchange gpsDeadLetterExchange() {
        return new DirectExchange(GPS_DLX_NAME, true, false); // durable, not auto-delete
    }

    // Step 2b: Define the Dead Letter Queue (DLQ) as a Spring Bean
    @Bean
    public Queue gpsDeadLetterQueue() {
        return new Queue(GPS_DLQ_NAME, true); // durable queue
    }

    // Step 2c: Bind the DLQ to the DLX
// Messages sent to gpsDeadLetterExchange with routing key GPS_DLQ_ROUTING_KEY
// will be routed to gpsDeadLetterQueue.
    @Bean
    public Binding bindingGpsDlqToDlx(Queue gpsDeadLetterQueue, DirectExchange gpsDeadLetterExchange) {
        return BindingBuilder.bind(gpsDeadLetterQueue)
                .to(gpsDeadLetterExchange)
                .with(GPS_DLQ_ROUTING_KEY); // Use the dedicated DLQ routing key
    }
}