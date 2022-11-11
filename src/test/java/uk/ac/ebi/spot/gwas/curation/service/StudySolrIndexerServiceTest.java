package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.gwas.curation.config.StudySolrIndexerServiceConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.*;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.solr.repository.StudySolrRepository;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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

    @Autowired
    StudySolrIndexerService studySolrIndexerService;


    @Before
    public void setup(){
        when(studyRepository.count()).thenReturn(new Long(12));
        when(diseaseTraitRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockDiseaseTrait()));
        when(efoTraitRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockEfoTrait()));
        when(noteRepository.findBySubmissionIdAndStudyTag(any(), any())).thenReturn((TestUtil.mockNote()));
        when(publicationRepository.findByPmid(any())).thenReturn(Optional.of(TestUtil.mockPublication()));
        when(studyIngestEntryRepository.save(any())).thenReturn(TestUtil.mockStudyIngestEntry());
        when(studyIngestEntryRepository.count()).thenReturn(new Long(12));
        when(studyIngestEntryRepository.findAll((Pageable) any())).thenReturn(TestUtil.mockStudyIngestEntries());
        when(studyRepository.findAll((Pageable) any())).thenReturn(TestUtil.mockStudies());
        when(studySolrRepository.save(any())).thenReturn(TestUtil.mockStudySolr());
        when(studySolrRepository.findBySubmissionIdAndAccessionId(any(), any())).thenReturn(Optional.of(TestUtil.mockStudySolr()));
        doNothing().when(studySolrRepository).delete(any());
        doNothing().when(studyIngestEntryRepository).delete(any());
    }

    @Test
    public void testPopulateStudyIngestEntries(){
        studySolrIndexerService.populateStudyIngestEntries();
    }

    @Test
    public void testReindexSolrStudies() {
        studySolrIndexerService.reindexSolrStudies();
    }

}
