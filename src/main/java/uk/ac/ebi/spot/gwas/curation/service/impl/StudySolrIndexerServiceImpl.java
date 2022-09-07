package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.*;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudySolrAssembler;
import uk.ac.ebi.spot.gwas.curation.service.StudySolrIndexerService;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.curation.solr.repository.StudySolrRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudySolrIndexerServiceImpl implements StudySolrIndexerService {

    private static final Logger log = LoggerFactory.getLogger(StudySolrIndexerServiceImpl.class);

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

    @Override
    //@Async
    public void populateStudyIngestEntries() {
        long cntStudies = studyRepository.count();

        long bucket = cntStudies/100;

        for( int i = 0; i <= bucket; i++ ) {
            log.info("Study Page running is "+ i);
            Pageable pageable = new PageRequest(i , 100);
            Page<Study> studies = studyRepository.findAll(pageable);
            List<Study> filterStudies = studies.stream().filter(study -> (study.getSubmissionId()!=null && !study.getSubmissionId().isEmpty()) && (study.getAccession() != null && !study.getAccession().isEmpty())).collect(Collectors.toList());
            filterStudies.forEach((study) -> {

                StudyIngestEntry studyIngestEntry = new StudyIngestEntry();
                studyIngestEntry.setAccessionId(study.getAccession());
                studyIngestEntry.setSubmissionId(study.getSubmissionId());
                studyIngestEntry.setReportedTrait(Optional.ofNullable(study.getDiseaseTrait())
                        .map((id) -> diseaseTraitRepository.findById(id))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(DiseaseTrait::getTrait).orElse(null));
                studyIngestEntry.setEfoTraits(study.getEfoTraits() != null ?
                                study.getEfoTraits().stream()
                                .map(efoId -> efoTraitRepository.findById(efoId))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(EfoTrait::getTrait)
                                .collect(Collectors.toList()) :null);
                studyIngestEntry.setGxeFlag(study.getGxeFlag());
                studyIngestEntry.setPooledFlag(study.getPooledFlag());
                studyIngestEntry.setSumstatsFlag(study.getSumstatsFlag());
                studyIngestEntry.setNotes(Optional.ofNullable
                        (noteRepository.findBySubmissionIdAndStudyTag(study.getSubmissionId(), study.getStudyTag()))
                        .filter(notes -> !notes.isEmpty())
                        .map(notes -> notes.stream()
                        .map(note -> note.getNote())
                        .collect(Collectors.toList())).orElse(null));

                Publication publication = Optional.ofNullable(study.getPmids())
                        .filter(pmids -> !pmids.isEmpty())
                        .map(pmids -> pmids.get(0))
                        .map(pmid -> publicationRepository.findByPmid(pmid))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .orElse(null);
                if(publication != null) {
                    studyIngestEntry.setFirstAuthor(publication.getFirstAuthor());
                    studyIngestEntry.setPublicationDate(publication.getPublicationDate());
                    studyIngestEntry.setTitle(publication.getTitle());
                    studyIngestEntry.setPmid(publication.getPmid());
                } else {
                 BodyOfWork bodyOfWork =   Optional.ofNullable(study.getBodyOfWorkList())
                            .filter(bows -> !bows.isEmpty() )
                            .map(bows -> bows.get(0))
                            .map(bowId -> bodyOfWorkRepository.findByBowId(bowId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .orElse(null);
                 if(bodyOfWork != null){
                     studyIngestEntry.setTitle(bodyOfWork.getTitle());
                     studyIngestEntry.setBowId(bodyOfWork.getBowId());
                 }
                }
                studyIngestEntryRepository.save(studyIngestEntry);
            });

        }
    }

    @Override
    @Async
    public void reindexSolrStudies() {
        long cntStudies = studyIngestEntryRepository.count();

        long bucket = cntStudies/100;
        for (int i = 0; i <= bucket; i++ ) {
            log.info("Solr Index Page running is "+ i);
            Pageable pageable = new PageRequest(i , 100);
            Page<StudyIngestEntry> studyIngestEntries = studyIngestEntryRepository.findAll(pageable);
            studyIngestEntries.forEach(studyIngestEntry -> createSolrStudy(studyIngestEntry));
        }
    }

    private void createSolrStudy(StudyIngestEntry studyIngestEntry) {
        StudySolr studySolr = StudySolrAssembler.assemble(studyIngestEntry);
        studySolrRepository.save(studySolr);
    }
}
