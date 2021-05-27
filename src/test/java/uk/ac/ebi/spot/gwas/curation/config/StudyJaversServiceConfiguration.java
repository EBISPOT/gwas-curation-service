package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.service.JaversCommonService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.service.StudyJaversService;
import uk.ac.ebi.spot.gwas.curation.service.impl.JaversCommonServiceImpl;
import uk.ac.ebi.spot.gwas.curation.service.impl.StudiesServiceImpl;
import uk.ac.ebi.spot.gwas.curation.service.impl.StudyJaversServiceImpl;

@TestConfiguration
public class StudyJaversServiceConfiguration {



    @Bean
    public StudyJaversService studyJaversService() {
        return new StudyJaversServiceImpl();
    }

    @Bean
    public JaversCommonService javersCommonService() {
        return new JaversCommonServiceImpl();
    }

    @Bean
    public StudiesService studiesService() {
        return new StudiesServiceImpl();
    }

}
