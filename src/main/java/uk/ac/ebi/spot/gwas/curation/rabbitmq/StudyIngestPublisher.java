package uk.ac.ebi.spot.gwas.curation.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.impl.StudySolrIndexerServiceImpl;
import uk.ac.ebi.spot.gwas.deposition.config.RabbitMQConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyIngestEntryDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyRabbitMessage;

@Component
public class StudyIngestPublisher {

    private static final Logger log = LoggerFactory.getLogger(StudyIngestPublisher.class);

    private RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitMQConfigProperties rabbitMQConfigProperties;

    public StudyIngestPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(StudyRabbitMessage studyRabbitMessage) {
        log.info("Sending Message for {] : {}",studyRabbitMessage.getSubmissionId(),studyRabbitMessage.getAccession());
        //rabbitTemplate.convertAndSend(DepositionCurationConstants.ROUTING_KEY, studyDto);

        rabbitTemplate.convertAndSend(rabbitMQConfigProperties.getExchangeName(),rabbitMQConfigProperties.getRoutingKey()
        , studyRabbitMessage);
    }
}
