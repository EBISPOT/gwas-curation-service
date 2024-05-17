package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.deposition.config.PublicationMQConfigProperties;

@Configuration
public class PublicationMQConfiguration {

    @Autowired
    PublicationMQConfigProperties publicationMQConfigProperties;

    @Bean
    Queue publicationQueue(){
        return new Queue(publicationMQConfigProperties.getPublicationQueueName(), true);
    }

    @Bean
    DirectExchange publicationExchange(){
        return new DirectExchange(publicationMQConfigProperties.getPublicationExchangeName());
    }

    @Bean
    Binding publicationBinding(Queue publicationQueue, DirectExchange publicationExchange) {
        return BindingBuilder.bind(publicationQueue).to(publicationExchange).with(publicationMQConfigProperties.getPublicationRoutingKey());
    }
}
