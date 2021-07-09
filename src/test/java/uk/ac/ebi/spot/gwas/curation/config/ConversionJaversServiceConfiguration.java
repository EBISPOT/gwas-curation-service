package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.curation.service.impl.*;

@TestConfiguration
public class ConversionJaversServiceConfiguration {

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

    @Bean
    public AssociationsService associationsService() {
        return new AssociationsServiceImpl();
    }

    @Bean
    public AssociationJaversService associationJaversService() {
        return new AssociationJaversServiceImpl();
    }

    @Bean
    public SampleJaversService sampleJaversService() {
        return new SampleJaversServiceImpl();
    }

    @Bean
    public SamplesService samplesService() {
        return new SamplesServiceImpl();
    }

    @Bean
    public FileUploadJaversService fileUploadJaversService() {
        return new FileUploadJaversServiceImpl();
    }

    @Bean
    public ConversionJaversService conversionJaversService() {
        return new ConversionJaversServiceImpl();
    }

    @Bean
    public FileUploadsService fileUploadsService() {
        return new FileUploadsServiceImpl();
    }
}
