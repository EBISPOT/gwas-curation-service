package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionDiffService;
import uk.ac.ebi.spot.gwas.curation.service.impl.SubmissionDiffServiceImpl;

@TestConfiguration
public class SubmissionDiffServiceConfiguration {

    @Bean
    public SubmissionDiffService submissionDiffService() {
        return new SubmissionDiffServiceImpl();
    }
}
