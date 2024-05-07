
package uk.ac.ebi.spot.gwas.curation.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MetadataYmlUpdate;

/**
This Consumer has been written for debugging purpose ,
In real scenario Python consumes the message for the
metadata Yaml hence we don't need to have this consumer
**/



@Profile({"local"})
@Component
public class MetadataYmlUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(MetadataYmlUpdateConsumer.class);

    @RabbitListener(queues = {DepositionCurationConstants.QUEUE_NAME_SUMSTATS_SANDBOX} )
    public void listen(MetadataYmlUpdate metadataYmlUpdate) {
        try {
            log.info("Consuming message for MetaYaml : {}",metadataYmlUpdate);

        } catch(Exception ex) {
            log.error("Error in consuming message"+ex.getMessage(),ex);
        }
    }
}


