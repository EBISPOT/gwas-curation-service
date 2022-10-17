package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.gwas.curation.repository.*;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.StudySolrIndexerService;
import uk.ac.ebi.spot.gwas.curation.service.impl.StudySolrIndexerServiceImpl;
import uk.ac.ebi.spot.gwas.curation.solr.repository.StudySolrRepository;

@TestConfiguration
public class StudySolrIndexerServiceConfiguration {

    @MockBean
    private StudyRepository studyRepository;

    @MockBean
    private DiseaseTraitRepository diseaseTraitRepository;

    @MockBean
    private EfoTraitRepository efoTraitRepository;

    @MockBean
    private NoteRepository noteRepository;

    @MockBean
    PublicationRepository publicationRepository;

    @MockBean
    StudyIngestEntryRepository studyIngestEntryRepository;

    @MockBean
    StudySolrRepository studySolrRepository;

    @MockBean
    BodyOfWorkRepository bodyOfWorkRepository;

    @MockBean
    StudyDtoAssembler studyDtoAssembler;

    @Bean
    public StudySolrIndexerService studySolrIndexerService(){
        return new StudySolrIndexerServiceImpl();
    }
}
