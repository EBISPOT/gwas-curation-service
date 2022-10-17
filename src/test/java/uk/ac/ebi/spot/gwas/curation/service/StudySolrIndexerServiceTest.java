package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.gwas.curation.config.StudySolrIndexerServiceConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.*;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.solr.repository.StudySolrRepository;

@RunWith(SpringRunner.class)
@Import(StudySolrIndexerServiceConfiguration.class)
public class StudySolrIndexerServiceTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private DiseaseTraitRepository diseaseTraitRepository;

    @Autowired
    private EfoTraitRepository efoTraitRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    PublicationRepository publicationRepository;

    @Autowired
    StudyIngestEntryRepository studyIngestEntryRepository;

    @Autowired
    StudySolrRepository studySolrRepository;

    @Autowired
    BodyOfWorkRepository bodyOfWorkRepository;

    @Autowired
    StudyDtoAssembler studyDtoAssembler;

    @Before
    public void setup(){


    }

}
