package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.deposition.config.RabbitMQConfigProperties;

@Configuration
public class MetaDataYmlConfiguration {

    @Autowired
    RabbitMQConfigProperties rabbitMQConfigProperties;

    @Bean
    Queue metadataymlqueue(){
        return new Queue(rabbitMQConfigProperties.getSumstatsQueueName(), true);
    }

    @Bean
    DirectExchange metadataymlexchange(){
        return new DirectExchange(rabbitMQConfigProperties.getSumstatsExchangeName());
    }

    @Bean
    Binding metadataymlbinding(Queue metadataymlqueue, DirectExchange metadataymlexchange) {
        return BindingBuilder.bind(metadataymlqueue).to(metadataymlexchange).with(rabbitMQConfigProperties.getSumstatsRoutingKey());
    }




}
