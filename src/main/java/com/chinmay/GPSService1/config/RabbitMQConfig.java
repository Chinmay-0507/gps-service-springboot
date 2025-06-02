package com.chinmay.GPSService1.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange; // We've chosen TopicExchange for flexibility
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

    // Optional: Constants for a Dead Letter Queue (DLQ) - good for error handling later
    // public static final String GPS_DLQ_NAME = "gps-data-dlq";
    // public static final String GPS_DLX_NAME = "gps-data-dlx"; // Dead Letter Exchange
    // public static final String GPS_DLQ_ROUTING_KEY = "gps.data.dead";

    // Step 3: Define the Queue as a Spring Bean
    @Bean // This annotation tells Spring that anf object will be created and should manage it (bean)
    public Queue gpsDataProcessingQueue() {
        // The constructor for Queue is: new Queue(String  name, boolean durable)
        // - name: The actual name of the queue on the RabbitMQ server.
        // - durable:
        //   - true: The queue will survive a RabbitMQ server restart (metadata is stored on disk).
        //           Messages published as 'persistent' to this queue will also survive.
        //   - false: The queue is transient and data will be lost if the server restarts.
        // For important data like GPS records, you almost always want durable queues.
        boolean durable = true;
        return new Queue(GPS_DATA_QUEUE_NAME, durable);
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
        //     - `*` (star) can substitute for exactly one word. (e.g., `gps.data.*` would match `gps.data.new` or `gps.data.update`)
        //     - `#` (hash) can substitute for zero or more words. (e.g., `gps.#` would match `gps.data.new` or `gps.update`)
        //   Here, we're using an exact routing key for simplicity in the producer.
        return BindingBuilder.bind(gpsDataProcessingQueue)
                .to(gpsDataTopicExchange)
                .with(GPS_DATA_ROUTING_KEY);
    }

    // Step 6: (Optional but Recommended) Configure a Message Converter for JSON
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

    // If you want to ensure RabbitTemplate uses this converter by default for all operations:
    // You can define your own RabbitTemplate bean. Spring Boot auto-configures one,
    // but defining your own gives you more control.
    /*
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory, final MessageConverter jsonMessageConverter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // Set the message converter for this template
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
    */
    // Note: ConnectionFactory is auto-configured by Spring Boot based on your application.properties.
}