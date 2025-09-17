package uk.ac.ebi.spot.gwas.curation.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.config.EFOTraitMQConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitRabbitMessage;

@Slf4j
@Component
public class EFOTraitMQProducer {

    private  RabbitTemplate rabbitTemplate;

    @Autowired
    EFOTraitMQConfigProperties efoTraitMQConfigProperties;

    public EFOTraitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(EfoTraitRabbitMessage efoTraitRabbitMessage) {
        log.info("the meassage for EFOtrait for Rabbit is {},{}", efoTraitRabbitMessage.getShortForm(), efoTraitRabbitMessage.getTrait());
        rabbitTemplate.convertAndSend(efoTraitMQConfigProperties.getEfoTraitExchangeName(),
                efoTraitMQConfigProperties.getEfoTraitRoutingKey(), efoTraitRabbitMessage);
    }
}
