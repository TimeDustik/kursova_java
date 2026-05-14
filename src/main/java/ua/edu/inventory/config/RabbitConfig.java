package ua.edu.inventory.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфігурація RabbitMQ: черги, exchange, bindings, JSON-конвертер.
 * Pattern: Singleton — всі AMQP-біни є синглтонами Spring.
 */
@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.queues.notification}")
    private String notificationQueue;

    @Value("${rabbitmq.queues.report}")
    private String reportQueue;

    @Value("${rabbitmq.exchanges.inventory}")
    private String inventoryExchange;

    @Value("${rabbitmq.routing-keys.notification}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.routing-keys.report}")
    private String reportRoutingKey;

    // ─── Черги ────────────────────────────────────────────────────────────────

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue).build();
    }

    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(reportQueue).build();
    }

    // ─── Exchange ─────────────────────────────────────────────────────────────

    @Bean
    public DirectExchange inventoryExchange() {
        return ExchangeBuilder.directExchange(inventoryExchange).durable(true).build();
    }

    // ─── Bindings ─────────────────────────────────────────────────────────────

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange inventoryExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(inventoryExchange)
                .with(notificationRoutingKey);
    }

    @Bean
    public Binding reportBinding(Queue reportQueue, DirectExchange inventoryExchange) {
        return BindingBuilder.bind(reportQueue)
                .to(inventoryExchange)
                .with(reportRoutingKey);
    }

    // ─── JSON-конвертер ────────────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
