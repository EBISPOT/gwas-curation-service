package uk.ac.ebi.spot.gwas.curation.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.config.DiseaseTraitMQConfigProperties;

import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitRabbitMessage;

@Slf4j
@Component
public class DiseaseTraitMQProducer {

    private RabbitTemplate rabbitTemplate;

    @Autowired
    DiseaseTraitMQConfigProperties diseaseTraitMQConfigProperties;

    public DiseaseTraitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(DiseaseTraitRabbitMessage diseaseTraitRabbitMessage) {
        log.info("the meassage for diseaseTrait for Rabbit is {}", diseaseTraitRabbitMessage.getTrait());
        rabbitTemplate.convertAndSend(diseaseTraitMQConfigProperties.getDiseasetraitExchangeName(),
                diseaseTraitMQConfigProperties.getDiseasetraitRoutingKey(), diseaseTraitRabbitMessage);
    }
}
