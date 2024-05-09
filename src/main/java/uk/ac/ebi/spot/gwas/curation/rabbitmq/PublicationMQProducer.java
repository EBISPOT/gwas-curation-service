package uk.ac.ebi.spot.gwas.curation.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.config.PublicationMQConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MetadataYmlUpdate;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationRabbitMessage;

@Component
public class PublicationMQProducer {

    private static final Logger log = LoggerFactory.getLogger(MetadataYmlUpdatePublisher.class);


    private RabbitTemplate rabbitTemplate;
    @Autowired
    PublicationMQConfigProperties publicationMQConfigProperties;

    public PublicationMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(PublicationRabbitMessage publicationRabbitMessage) {
        log.info("the meassage for Publication for Rabbit is {}",publicationRabbitMessage);
        rabbitTemplate.convertAndSend(publicationMQConfigProperties.getPublicationExchangeName(),
                publicationMQConfigProperties.getPublicationRoutingKey()
        , publicationRabbitMessage);
    }
}
