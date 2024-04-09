package uk.ac.ebi.spot.gwas.curation.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.StudySolrIndexerService;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyRabbitMessage;

@Component
public class StudyIngestConsumer {

    private static final Logger log = LoggerFactory.getLogger(StudyIngestConsumer.class);

    @Autowired
    StudySolrIndexerService studySolrIndexerService;

    @Autowired
    StudyDtoAssembler studyDtoAssembler;


    @RabbitListener(queues = { DepositionCurationConstants.QUEUE_NAME_SANDBOX,
            DepositionCurationConstants.QUEUE_NAME_PROD } )
     public void listen(StudyRabbitMessage studyRabbitMessage) {
        try {
            log.info("Consuming message for {} : {}", studyRabbitMessage.getSubmissionId(), studyRabbitMessage.getAccession());
            studySolrIndexerService.syncSolrWithStudies(studyRabbitMessage);
        } catch(Exception ex) {
            log.error("Error in consuming message"+ex.getMessage(),ex);
        }
    }
}
