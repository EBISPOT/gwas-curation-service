package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.service.AssociationJaversService;
import uk.ac.ebi.spot.gwas.curation.service.AssociationsService;
import uk.ac.ebi.spot.gwas.curation.service.JaversCommonService;
import uk.ac.ebi.spot.gwas.curation.service.impl.AssociationJaversServiceImpl;
import uk.ac.ebi.spot.gwas.curation.service.impl.AssociationsServiceImpl;
import uk.ac.ebi.spot.gwas.curation.service.impl.JaversCommonServiceImpl;

@TestConfiguration
public class AssociationJaversServiceConfiguration {

    @Bean
    public JaversCommonService javersCommonService() {
        return new JaversCommonServiceImpl();
    }

    @Bean
    public AssociationsService associationsService() {
        return new AssociationsServiceImpl();
    }

    @Bean
    public AssociationJaversService associationJaversService() {
        return new AssociationJaversServiceImpl();
    }
}
