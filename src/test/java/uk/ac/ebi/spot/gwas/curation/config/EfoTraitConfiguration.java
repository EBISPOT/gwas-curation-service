package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.EfoTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.ProvenanceDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.service.impl.EfoTraitServiceImpl;

@TestConfiguration
public class EfoTraitConfiguration {

    @MockBean
    EfoTraitRepository efoTraitRepository;

    @MockBean
    StudyRepository studyRepository;

    @MockBean
    UserService userService;

    @MockBean
    JWTService jwtService;


    @MockBean
    EfoTraitDtoAssembler efoTraitDtoAssembler;

    @MockBean
    DepositionCurationConfig depositionCurationConfig;

    @MockBean
    ProvenanceDtoAssembler provenanceDtoAssembler;

    @Bean
    public EfoTraitService efoTraitService() {
        return new EfoTraitServiceImpl(efoTraitRepository, studyRepository, efoTraitDtoAssembler);
    }
}
