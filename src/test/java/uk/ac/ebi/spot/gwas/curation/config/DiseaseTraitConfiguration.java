package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.ProvenanceDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.service.impl.DiseaseTraitServiceImpl;

@TestConfiguration
public class DiseaseTraitConfiguration {

    @MockBean
    DiseaseTraitRepository diseaseTraitRepository;

    @MockBean
    StudyRepository studyRepository;

    @MockBean
    UserService userService;

    @MockBean
    JWTService jwtService;


    @MockBean
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @MockBean
    DepositionCurationConfig depositionCurationConfig;

    @MockBean
    ProvenanceDtoAssembler provenanceDtoAssembler;

    @Bean
    public DiseaseTraitService diseaseTraitService() { return new DiseaseTraitServiceImpl(diseaseTraitRepository);  }
}
