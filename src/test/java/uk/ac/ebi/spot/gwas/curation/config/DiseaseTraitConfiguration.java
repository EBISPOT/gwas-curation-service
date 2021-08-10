package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.impl.DiseaseTraitServiceImpl;

@TestConfiguration
public class DiseaseTraitConfiguration {

    @MockBean
    DiseaseTraitRepository diseaseTraitRepository;

    @MockBean
    StudyRepository studyRepository;

    @Bean
    public DiseaseTraitService diseaseTraitService() { return new DiseaseTraitServiceImpl(diseaseTraitRepository);  }
}
