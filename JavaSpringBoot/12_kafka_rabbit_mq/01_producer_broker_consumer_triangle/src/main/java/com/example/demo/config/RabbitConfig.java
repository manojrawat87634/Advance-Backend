package com.example.demo.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.exchange.name:order.exchange}")
    private String exchange;

    @Value("${app.queue.email:email.queue}")
    private String emailQueue;

    @Value("${app.queue.inventory:inventory.queue}")
    private String inventoryQueue;

    @Value("${app.routing.key:order.created}")
    private String routingKey;

    // 1. Create Queues
    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue, true);
    }

    @Bean
    public Queue inventoryQueue() {
        return new Queue(inventoryQueue, true);
    }

    // 2. Create Topic Exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    // 3. Bind Email Queue to Exchange
    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailQueue).to(exchange).with(routingKey);
    }

    // 4. Bind Inventory Queue to Exchange
    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, TopicExchange exchange) {
        return BindingBuilder.bind(inventoryQueue).to(exchange).with(routingKey);
    }

    // 5. Automatically convert Java objects to JSON format when sending
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}