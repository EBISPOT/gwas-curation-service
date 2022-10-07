package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.deposition.config.RabbitMQConfigProperties;

@Configuration
public class MyRabbitConfiguration {

    @Autowired
    RabbitMQConfigProperties rabbitMQConfigProperties;

    @Bean
    Queue queue(){
        return new Queue(rabbitMQConfigProperties.getQueueName(), true);
    }

    @Bean
    DirectExchange exchange(){
        return new DirectExchange(rabbitMQConfigProperties.getExchangeName());
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(rabbitMQConfigProperties.getRoutingKey());
    }




}
