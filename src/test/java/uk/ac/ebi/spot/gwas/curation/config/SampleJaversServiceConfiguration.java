package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.service.JaversCommonService;
import uk.ac.ebi.spot.gwas.curation.service.SampleJaversService;
import uk.ac.ebi.spot.gwas.curation.service.SamplesService;
import uk.ac.ebi.spot.gwas.curation.service.impl.JaversCommonServiceImpl;
import uk.ac.ebi.spot.gwas.curation.service.impl.SampleJaversServiceImpl;
import uk.ac.ebi.spot.gwas.curation.service.impl.SamplesServiceImpl;

@TestConfiguration
public class SampleJaversServiceConfiguration {

    @Bean
    public JaversCommonService javersCommonService() {
        return new JaversCommonServiceImpl();
    }

    @Bean
    public SampleJaversService sampleJaversService() {
        return new SampleJaversServiceImpl();
    }

    @Bean
    public SamplesService samplesService() {
        return new SamplesServiceImpl();
    }
}
