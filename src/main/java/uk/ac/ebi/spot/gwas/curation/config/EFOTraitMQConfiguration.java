package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.config.EFOTraitMQConfigProperties;

@Component
public class EFOTraitMQConfiguration {

    @Autowired
    EFOTraitMQConfigProperties efoTraitMQConfigProperties;

    @Bean
    Queue efoTraitQueue() {
        return new Queue(efoTraitMQConfigProperties.getEfoTraitQueueName(), true);
    }

    @Bean
    DirectExchange efoTraitExchange() {
        return new DirectExchange(efoTraitMQConfigProperties.getEfoTraitExchangeName());
    }

    @Bean
    Binding efoTraitBinding(Queue efoTraitQueue, DirectExchange efoTraitExchange) {
        return BindingBuilder.bind(efoTraitQueue).to(efoTraitExchange).with(efoTraitMQConfigProperties.getEfoTraitRoutingKey());
    }

}
