package uk.ac.ebi.spot.gwas.curation.service.impl;

//import com.querydsl.core.types.Predicate;
import com.mongodb.bulk.BulkWriteResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.rabbitmq.MetadataYmlUpdatePublisher;
import uk.ac.ebi.spot.gwas.curation.rabbitmq.StudyIngestPublisher;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.*;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.curation.solr.repository.StudySolrRepository;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
//import uk.ac.ebi.spot.gwas.deposition.domain.QStudy;
//import uk.ac.ebi.spot.gwas.deposition.domain.QStudy;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StudiesServiceImpl implements StudiesService {

    private static final Logger log = LoggerFactory.getLogger(StudiesService.class);

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private DiseaseTraitService diseaseTraitService;

    @Autowired
    private EfoTraitService efoTraitService;

    @Autowired
    StudySampleDescPatchRequestAssembler studySampleDescPatchRequestAssembler;

    @Autowired
    private DiseaseTraitRepository diseaseTraitRepository;

    @Autowired
    private EfoTraitRepository efoTraitRepository;

    @Autowired
    private FileHandler fileHandler;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    StudySolrRepository studySolrRepository;

    @Autowired
    StudyIngestPublisher studyIngestPublisher;

    @Autowired
    MetadataYmlUpdatePublisher metadataYmlUpdatePublisher;

    @Autowired
    StudyRabbitMessageAssembler studyRabbitMessageAssembler;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @Autowired
    EfoTraitDtoAssembler efoTraitDtoAssembler;

    @Override
    public Study updateStudies(Study study) {
        log.info("Inside updateStudies");
        Study updatedStudy = studyRepository.save(study);
        sendStudyChangeMessage(updatedStudy);
        metadataYmlUpdatePublisher.send(MetadataYmlUpdate.builder()
                .args(Collections.singletonList(study.getAccession()))
                .task("sumstats_service.app.convert_metadata_to_yaml")
                .id(UUID.randomUUID().toString()).build());
        return updatedStudy;
    }

    @Override
    public Study getStudy(String studyId) {
        //log.info("Retrieving study: {}", studyId);
        Optional<Study> studyOptional = studyRepository.findById(studyId);
        if (studyOptional.isPresent()) {
            //log.info("Found study: {}", studyOptional.get().getStudyTag());
            return studyOptional.get();
        }
        log.error("Unable to find study: {}", studyId);
        return null;
    }



    @Override
    public Page<Study> getStudies(String submissionId,  Pageable page) {

        return studyRepository.findBySubmissionId(submissionId, page);
    }


    public void sendMetaDataMessageToQueue(String submissionId) {
      Long studiesCount =  studyRepository.findBySubmissionId(submissionId).count();
      log.info("Studies count for {} is {}",submissionId,  studiesCount);
      long bucket = studiesCount / 100;
      log.info("Bucket size for studies is {}",bucket);
      for (int i = 0; i <= bucket; i++) {
            log.info("Sending Studies to Queue Page running is {}", i);
            Pageable pageable = new PageRequest(i, 100);
            Page<Study> studies = studyRepository.findBySubmissionId(submissionId, pageable);
            log.info("Studies size is -> {}",studies.getTotalElements());
            studies.forEach(study -> metadataYmlUpdatePublisher.send( MetadataYmlUpdate.builder()
                                                                        .args(Collections.singletonList(study.getAccession()))
                                                                        .task("sumstats_service.app.convert_metadata_to_yaml")
                                                                         .id(UUID.randomUUID().toString()).build()));
      }
    }

    @Override
    public Page<StudySolr> getStudies(Pageable page, SearchStudyDTO searchStudyDTO) {

        if(searchStudyDTO != null) {

            Boolean sumStatsFlag = searchStudyDTO.getSumstatsFlag();
            Boolean pooledFlag = searchStudyDTO.getPooledFlag();
            Boolean gxeFlag = searchStudyDTO.getGxeFlag();
            String efo = searchStudyDTO.getEfoTrait();
            String reportedTrait = searchStudyDTO.getReportedTrait();
            String note = searchStudyDTO.getNote();

            if (searchStudyDTO.getAccessionId() != null) {
                return studySolrRepository.findByAccessionId(searchStudyDTO.getAccessionId(), page);
            } else if (efo != null && reportedTrait != null && note != null &&
                    sumStatsFlag != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlagOrGxeFlagOrPooledFlag(efo, reportedTrait, note,
                        sumStatsFlag, gxeFlag, pooledFlag, page);
            } else if (efo != null && reportedTrait != null && note != null &&
                    sumStatsFlag != null && pooledFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlagOrPooledFlag(efo, reportedTrait, note,
                        sumStatsFlag, pooledFlag, page);
            } else if (efo != null && reportedTrait != null && note != null &&
                    sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlagOrGxeFlag(efo, reportedTrait, note,
                        sumStatsFlag, gxeFlag, page);
            } else if (efo != null && reportedTrait != null && note != null &&
                    pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotesAndPooledFlagOrGxeFlag(efo, reportedTrait, note,
                        pooledFlag, gxeFlag, page);
            } else if (efo != null && reportedTrait != null && note != null &&
                    sumStatsFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlag(efo, reportedTrait, note,
                        sumStatsFlag, page);
            } else if (efo != null && reportedTrait != null && note != null &&
                    pooledFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotesAndPooledFlag(efo, reportedTrait, note,
                        pooledFlag, page);
            } else if (efo != null && reportedTrait != null && note != null &&
                    gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotesAndGxeFlag(efo, reportedTrait, note,
                        gxeFlag, page);
            } else if (efo != null && reportedTrait != null
                    && note != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndNotes(efo, reportedTrait, note, page);
            } else if (efo != null && note != null && pooledFlag != null
                    && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndNotesAndSumstatsFlagOrGxeFlagOrPooledFlag(efo
                        , note, pooledFlag, sumStatsFlag, gxeFlag, page);
            } else if (efo != null && note != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndNotesAndSumstatsFlagOrGxeFlag(efo,
                        note, sumStatsFlag, gxeFlag, page);
            } else if (efo != null && note != null && sumStatsFlag != null && pooledFlag != null) {
                return studySolrRepository.findByEfoTraitsAndNotesAndSumstatsFlagOrPooledFlag(efo,
                        note, sumStatsFlag, pooledFlag, page);
            } else if (efo != null && note != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndNotesAndPooledFlagOrGxeFlag(efo,
                        note, sumStatsFlag, pooledFlag, page);
            } else if (efo != null && note != null && pooledFlag != null) {
                return studySolrRepository.findByEfoTraitsAndNotesAndPooledFlag(efo,
                        note, pooledFlag, page);
            } else if (efo != null && note != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndNotesAndGxeFlag(efo,
                        note, gxeFlag, page);
            } else if (efo != null && note != null && sumStatsFlag != null) {
                return studySolrRepository.findByEfoTraitsAndNotesAndSumstatsFlag(efo,
                        note, sumStatsFlag, page);
            } else if (efo != null && note != null) {
                return studySolrRepository.findByEfoTraitsAndNotes(efo,
                        note, page);
            }else if (reportedTrait != null && note != null && pooledFlag != null
                    && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndNotesAndSumstatsFlagOrGxeFlagOrPooledFlag(reportedTrait
                        , note, pooledFlag, sumStatsFlag, gxeFlag, page);
            } else if (reportedTrait != null && note != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndNotesAndSumstatsFlagOrGxeFlag(reportedTrait,
                        note, sumStatsFlag, gxeFlag, page);
            } else if (reportedTrait != null && note != null && sumStatsFlag != null && pooledFlag != null) {
                return studySolrRepository.findByReportedTraitAndNotesAndSumstatsFlagOrPooledFlag(reportedTrait,
                        note, sumStatsFlag, pooledFlag, page);
            } else if (reportedTrait != null && note != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndNotesAndPooledFlagOrGxeFlag(reportedTrait,
                        note, pooledFlag, gxeFlag, page);
            } else if (reportedTrait != null && note != null && pooledFlag != null) {
                return studySolrRepository.findByReportedTraitAndNotesAndPooledFlag(reportedTrait,
                        note, pooledFlag, page);
            } else if (reportedTrait != null && note != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndNotesAndGxeFlag(reportedTrait,
                        note, gxeFlag, page);
            } else if (reportedTrait != null && note != null && sumStatsFlag != null) {
                return studySolrRepository.findByReportedTraitAndNotesAndSumstatsFlag(reportedTrait,
                        note, sumStatsFlag, page);
            } else if (reportedTrait != null && note != null) {
                return studySolrRepository.findByReportedTraitAndNotes(reportedTrait,
                        note, page);
            } else if (efo != null && reportedTrait != null && sumStatsFlag != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndSumstatsFlagOrGxeFlagOrPooledFlag(efo, reportedTrait,
                        sumStatsFlag, gxeFlag, pooledFlag, page);
            } else if (efo != null && reportedTrait != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndSumstatsFlagOrGxeFlag(efo, reportedTrait,
                        sumStatsFlag, gxeFlag, page);
            } else if (efo != null && reportedTrait != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndPooledFlagOrGxeFlag(efo, reportedTrait,
                        pooledFlag, gxeFlag, page);
            } else if (efo != null && reportedTrait != null && pooledFlag != null && sumStatsFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndPooledFlagOrSumstatsFlag(efo, reportedTrait,
                        pooledFlag, sumStatsFlag, page);
            } else if (efo != null && reportedTrait != null && pooledFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndPooledFlag(efo, reportedTrait,
                        pooledFlag, page);
            } else if (efo != null && reportedTrait != null && sumStatsFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndSumstatsFlag(efo, reportedTrait,
                        sumStatsFlag, page);
            } else if (efo != null && reportedTrait != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTraitAndGxeFlag(efo, reportedTrait,
                        gxeFlag, page);
            } else if (efo != null && pooledFlag != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndSumstatsFlagOrGxeFlagOrPooledFlag(efo
                        , pooledFlag, sumStatsFlag, gxeFlag, page);
            } else if (efo != null && pooledFlag != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndSumstatsFlagOrGxeFlag(efo
                        , sumStatsFlag, gxeFlag, page);
            } else if (efo != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndPooledFlagOrGxeFlag(efo
                        , pooledFlag, gxeFlag, page);
            } else if (efo != null && pooledFlag != null && sumStatsFlag != null) {
                return studySolrRepository.findByEfoTraitsAndPooledFlagOrSumstatsFlag(efo
                        , pooledFlag, sumStatsFlag, page);
            } else if (efo != null && pooledFlag != null) {
                return studySolrRepository.findByEfoTraitsAndPooledFlag(efo
                        , pooledFlag, page);
            } else if (efo != null && sumStatsFlag != null) {
                return studySolrRepository.findByEfoTraitsAndSumstatsFlag(efo
                        , pooledFlag, page);
            } else if (efo != null && gxeFlag != null) {
                return studySolrRepository.findByEfoTraitsAndGxeFlag(efo
                        , gxeFlag, page);
            } else if (reportedTrait != null &&
                    pooledFlag != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndSumstatsFlagOrGxeFlagOrPooledFlag(reportedTrait
                        , pooledFlag, sumStatsFlag, gxeFlag, page);
            } else if (reportedTrait != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndSumstatsFlagOrGxeFlag(reportedTrait
                        , sumStatsFlag, gxeFlag, page);
            } else if (reportedTrait != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndPooledFlagOrGxeFlag(reportedTrait
                        , pooledFlag, gxeFlag, page);
            } else if (reportedTrait != null && pooledFlag != null && sumStatsFlag != null) {
                return studySolrRepository.findByReportedTraitAndPooledFlagOrSumstatsFlag(reportedTrait
                        , pooledFlag, sumStatsFlag, page);
            } else if (reportedTrait != null && pooledFlag != null) {
                return studySolrRepository.findByReportedTraitAndPooledFlag(reportedTrait
                        , pooledFlag, page);
            } else if (reportedTrait != null && sumStatsFlag != null) {
                return studySolrRepository.findByReportedTraitAndSumstatsFlag(reportedTrait
                        , sumStatsFlag, page);
            } else if (reportedTrait != null && gxeFlag != null) {
                return studySolrRepository.findByReportedTraitAndGxeFlag(reportedTrait
                        , gxeFlag, page);
            } else if (note != null && sumStatsFlag != null && gxeFlag != null && pooledFlag != null) {
                return studySolrRepository.findByNotesAndSumstatsFlagOrGxeFlagOrPooledFlag(note, sumStatsFlag, gxeFlag, pooledFlag, page);
            } else if (note != null && sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findByNotesAndSumstatsFlagOrGxeFlag(note, sumStatsFlag, gxeFlag, page);
            } else if (note != null && sumStatsFlag != null && pooledFlag != null) {
                return studySolrRepository.findByNotesAndSumstatsFlagOrPooledFlag(note, sumStatsFlag, pooledFlag, page);
            } else if (note != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByNotesAndPooledFlagOrGxeFlag(note, pooledFlag, gxeFlag, page);
            } else if (note != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByNotesAndPooledFlagOrGxeFlag(note, pooledFlag, gxeFlag, page);
            } else if (note != null && pooledFlag != null) {
                return studySolrRepository.findByNotesAndPooledFlag(note, pooledFlag, page);
            } else if (note != null && gxeFlag != null) {
                return studySolrRepository.findByNotesAndGxeFlag(note, gxeFlag, page);
            } else if (note != null && sumStatsFlag != null) {
                return studySolrRepository.findByNotesAndSumstatsFlag(note, sumStatsFlag, page);
            } else if (efo != null && reportedTrait != null) {
                return studySolrRepository.findByEfoTraitsAndReportedTrait(efo, reportedTrait, page);
            } else if (efo != null) {
                if(efo.contains(" "))
                    return studySolrRepository.findByEfoTraits(efo, page);
                else
                    return studySolrRepository.findByEfoTraitsWithoutSpaces(efo, page);
            } else if (reportedTrait != null) {
                if(reportedTrait.contains(" "))
                    return studySolrRepository.findByReportedTrait(reportedTrait, page);
                else
                    return studySolrRepository.findByReportedTraitWithoutSpaces(reportedTrait, page);
            } else if (searchStudyDTO.getPmid() != null) {
                return studySolrRepository.findByPmid(searchStudyDTO.getPmid(), page);
            } else if (searchStudyDTO.getSubmissionId() != null) {
                return studySolrRepository.findBySubmissionId(searchStudyDTO.getSubmissionId(), page);
            } else if (searchStudyDTO.getBowId() != null) {
                return studySolrRepository.findByBowId(searchStudyDTO.getBowId(), page);
            } else if (sumStatsFlag != null && pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findBySumstatsFlagOrPooledFlagOrGxeFlag(sumStatsFlag, pooledFlag, gxeFlag, page);
            } else if (sumStatsFlag != null && pooledFlag != null) {
                return studySolrRepository.findBySumstatsFlagOrPooledFlag(sumStatsFlag, pooledFlag, page);
            } else if (pooledFlag != null && gxeFlag != null) {
                return studySolrRepository.findByPooledFlagOrGxeFlag(pooledFlag, gxeFlag, page);
            } else if (sumStatsFlag != null && gxeFlag != null) {
                return studySolrRepository.findBySumstatsFlagOrGxeFlag(sumStatsFlag, gxeFlag, page);
            } else if (sumStatsFlag != null) {
                return studySolrRepository.findBySumstatsFlag(sumStatsFlag, page);
            } else if (pooledFlag != null) {
                return studySolrRepository.findByPooledFlag(pooledFlag, page);
            } else if (gxeFlag != null) {
                return studySolrRepository.findByGxeFlag(gxeFlag, page);
            } else if (note != null) {
                log.info("Note Param ->" + note);
                if(note.contains(" "))
                    return studySolrRepository.findByNotes(note, page);
                else
                    return studySolrRepository.findByNotesWithoutSpaces(note, page);
            }
        }
        return studySolrRepository.findAll(page);

    }

    public DiseaseTrait getDiseaseTraitsByStudyId(String studyId) {
        String  traitId = getStudy(studyId).getDiseaseTrait();

        Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitRepository.findById(traitId);
        if(optionalDiseaseTrait.isPresent())
            return optionalDiseaseTrait.get();
        else
            return null;

    }

    @Override
    public Study getStudyByAccession(String accessionId, String submissionId) {
        log.info("Retrieving study from accession: {}", accessionId);
        Optional<Study> studyOptional = studyRepository.findByAccessionAndSubmissionId(accessionId, submissionId);
        if (studyOptional.isPresent()) {
            log.info("Found study: {}", studyOptional.get().getStudyTag());
            return studyOptional.get();
        }
        log.error("Unable to find study with : {}", accessionId);
        return null;
    }


    @Override
    public UploadReportWrapper updateMultiTraitsForStudies(List<MultiTraitStudyMappingDto> multiTraitStudyMappingDtos, String submissionId) {
        log.info("Inside updateMultiTraitsForStudies()");

        Set<String> shortForms = new HashSet<>();
        Set<String> backgroundShortForms = new HashSet<>();
        Set<String> reportedTraits = new HashSet<>();
        multiTraitStudyMappingDtos.forEach(multiTraitStudyMappingDto -> {
            shortForms.addAll(Arrays.asList(StringUtils.deleteWhitespace(multiTraitStudyMappingDto.getEfoTraitShortForm()).split("\\|")));
            backgroundShortForms.addAll(Arrays.asList(StringUtils.deleteWhitespace(multiTraitStudyMappingDto.getBackgroundEfoShortForm()).split("\\|")));
            reportedTraits.add(multiTraitStudyMappingDto.getReportedTrait());
        });



        Map<String, EfoTrait> retrievedEfoTraits = efoTraitRepository.findByShortFormIn(shortForms).collect(Collectors.toMap(EfoTrait::getShortForm, e -> e));
        Map<String, EfoTrait> retrievedBackgroundEfoTraits = efoTraitRepository.findByShortFormIn(backgroundShortForms).collect(Collectors.toMap(EfoTrait::getShortForm, e -> e));
        Map<String, DiseaseTrait> retrievedReportedTraits = diseaseTraitRepository.findByTraitIgnoreCaseIn(reportedTraits).collect(Collectors.toMap(DiseaseTrait::getTrait, d -> d));
        Map<String, Study> studies = studyRepository.findBySubmissionId(submissionId).collect(Collectors.toMap(Study::getAccession, s -> s));
        Map<String, Study> studiesToSave = new HashMap<>();
        List<MultiTraitStudyMappingReport> report = new ArrayList<>();
        UploadReportWrapper uploadReportWrapper = new UploadReportWrapper();

        multiTraitStudyMappingDtos.forEach(multiTraitStudyMappingDto -> {
            boolean invalidStudyTag = false;
            Study study = studies.get(multiTraitStudyMappingDto.getGcst().trim());
            if (study == null) {
                uploadReportWrapper.setHasErrors(true);
                report.add(new MultiTraitStudyMappingReport(multiTraitStudyMappingDto.getGcst(), multiTraitStudyMappingDto.getStudyTag(), "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag."));
            }
            else {
                if(!multiTraitStudyMappingDto.getStudyTag().trim().equalsIgnoreCase(study.getStudyTag())) {
                    //log.info("Inside invalidStudyTag block()"+study.getStudyTag());
                    invalidStudyTag = true;
                }

                if (!invalidStudyTag) {
                    //log.info("Inside validStudyTag block()"+study.getStudyTag());
                    String efoTraitComments = "";
                    HashSet<String> newStudyEfos = new HashSet<>(Arrays.asList(StringUtils.deleteWhitespace(multiTraitStudyMappingDto.getEfoTraitShortForm().trim()).split("\\|")));
                    ArrayList<String> studyEfoTraitsIds = new ArrayList<>();
                    ArrayList<String> studyEfoTraitsShortForms = new ArrayList<>();
                    for (String shortForm : newStudyEfos) {
                        if (retrievedEfoTraits.containsKey(shortForm.trim())) {
                            EfoTrait efoTrait = retrievedEfoTraits.get(shortForm.trim());
                            studyEfoTraitsIds.add(efoTrait.getId());
                            studyEfoTraitsShortForms.add(efoTrait.getShortForm());
                        } else {
                            uploadReportWrapper.setHasErrors(true);
                            efoTraitComments = efoTraitComments.concat("\n" + shortForm + " not found in DB.");
                        }
                    }
                    study.setEfoTraits(studyEfoTraitsIds);
                    efoTraitComments = efoTraitComments.concat("\nCurrent: " + StringUtils.join(studyEfoTraitsShortForms, "|"));

                    String backgroundEfoTraitComments = "";
                    HashSet<String> newBackgroundStudyEfos = new HashSet<>(Arrays.asList(StringUtils.deleteWhitespace(multiTraitStudyMappingDto.getBackgroundEfoShortForm().trim()).split("\\|")));
                    ArrayList<String> studyBackgroundEfoTraitsIds = new ArrayList<>();
                    ArrayList<String> studyBackgroundEfoTraitsShortForms = new ArrayList<>();
                    for (String shortForm : newBackgroundStudyEfos) {
                        if (retrievedBackgroundEfoTraits.containsKey(shortForm.trim())) {
                            EfoTrait efoTrait = retrievedBackgroundEfoTraits.get(shortForm.trim());
                            studyBackgroundEfoTraitsIds.add(efoTrait.getId());
                            studyBackgroundEfoTraitsShortForms.add(efoTrait.getShortForm());
                        } else {
                            uploadReportWrapper.setHasErrors(true);
                            backgroundEfoTraitComments = backgroundEfoTraitComments.concat("\n" + shortForm + " not found in DB.");
                        }
                    }
                    study.setBackgroundEfoTraits(studyBackgroundEfoTraitsIds);
                    backgroundEfoTraitComments = backgroundEfoTraitComments.concat("\nCurrent: " + StringUtils.join(studyBackgroundEfoTraitsShortForms, "|"));

                    studiesToSave.put(study.getId(), study);

                    String reportedTraitComments = "";
                    if (retrievedReportedTraits.containsKey(multiTraitStudyMappingDto.getReportedTrait().trim())) {
                        //log.info("Reported Trait in file:"+multiTraitStudyMappingDto.getReportedTrait().trim());
                        //log.info("GCST currently being analysed:"+study.getAccession());
                        DiseaseTrait diseaseTrait = retrievedReportedTraits.get(multiTraitStudyMappingDto.getReportedTrait().trim());
                        //log.info("Disease Trait Id being assigned :"+diseaseTrait.getId());
                        study.setDiseaseTrait(diseaseTrait.getId());
                        studiesToSave.put(study.getId(), study);
                        reportedTraitComments = reportedTraitComments.concat("Reported trait set to: " + diseaseTrait.getTrait());
                    } else {
                        uploadReportWrapper.setHasErrors(true);
                        reportedTraitComments = reportedTraitComments.concat("Reported trait " + multiTraitStudyMappingDto.getReportedTrait() + " not found in DB");
                    }
                    report.add(new MultiTraitStudyMappingReport(study.getAccession(), study.getStudyTag(), efoTraitComments.trim(), backgroundEfoTraitComments.trim(), reportedTraitComments.trim()));
                }
                else {
                    uploadReportWrapper.setHasErrors(true);
                    report.add(new MultiTraitStudyMappingReport(multiTraitStudyMappingDto.getGcst(), multiTraitStudyMappingDto.getStudyTag(), "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag."));
                }
            }
        });
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Study.class);
        for (Study study: studiesToSave.values()) {
            //log.info("Study GCST which are bulkuploaded -:"+study.getAccession());
            //log.info("Disease Trait Id being assigned to study is :"+study.getDiseaseTrait());
            Query query = new Query().addCriteria(new Criteria("id").is(study.getId()));
            Update update = new Update()
                    .set("efoTraits", study.getEfoTraits())
                    .set("backgroundEfoTraits", study.getBackgroundEfoTraits())
                    .set("diseaseTrait", study.getDiseaseTrait());
            bulkOps.updateOne(query, update);
            sendStudyChangeMessage(study);
        }
        if (!studiesToSave.isEmpty()) {
            bulkOps.execute();
            //log.info("Bule write Result"+bulkWriteResult.getUpserts());
        }
        uploadReportWrapper.setUploadReport(fileHandler.serializePojoToTsv(report));
        return uploadReportWrapper;
    }

    @Override
    public List<StudySampleDescPatchRequest> updateSampleDescription(List<StudySampleDescPatchRequest> studySampleDescPatchRequests, String submissionId) {
        return studySampleDescPatchRequests.stream().map((studySampleDescPatchRequest) ->
                Optional.ofNullable(getStudyByAccession(studySampleDescPatchRequest.getGcst().trim(), submissionId))
                        .map(study -> studySampleDescPatchRequestAssembler.disassemble(studySampleDescPatchRequest, study.getId()))
                        .map(this::updateStudies)
                        .map(studySampleDescPatchRequestAssembler::assemble).orElse(null)
        ).collect(Collectors.toList());

    }

    @Override
    public byte[] uploadSampleDescriptions(List<StudySampleDescPatchRequest> studySampleDescPatchRequests, String submissionId) {
        AtomicInteger initialSampleDescCnt = new AtomicInteger();
        AtomicInteger replicatedSampleDescCnt = new AtomicInteger();
        StringBuilder sampleChangesBuilder = new StringBuilder();
        StringBuilder finalUploadBuilder = new StringBuilder();
        Map<String, Study> studyMap = studyRepository.findBySubmissionId(submissionId).collect(Collectors.toMap(Study::getAccession, s -> s));
        studySampleDescPatchRequests.forEach((studySampleDescPatchRequest) ->
        {
            boolean invalidStudyTag = false;
            boolean sampleDescChanged = false;
            //Study study = getStudyByAccession(studySampleDescPatchRequest.getGcst(), submissionId);
            Study study = studyMap.get(studySampleDescPatchRequest.getGcst());
            if(study != null && !studySampleDescPatchRequest.getStudyTag().equalsIgnoreCase(study.getStudyTag()))
                invalidStudyTag = true;
            if(study != null && !invalidStudyTag) {
                if(studySampleDescPatchRequest.getInitialSampleDescription() != null && !study.getInitialSampleDescription().equalsIgnoreCase(studySampleDescPatchRequest.getInitialSampleDescription())) {
                    initialSampleDescCnt.getAndIncrement();
                    study.setInitialSampleDescription(studySampleDescPatchRequest.getInitialSampleDescription());
                    sampleDescChanged = true;
                    sampleChangesBuilder.append("Initial Sample Description changed successfully for GCST "+study.getAccession() + " and Study Tag "+study.getStudyTag());
                    sampleChangesBuilder.append("\n");
                }
                if(studySampleDescPatchRequest.getReplicateSampleDescription() != null && !study.getReplicateSampleDescription().equalsIgnoreCase(studySampleDescPatchRequest.getReplicateSampleDescription())) {
                    replicatedSampleDescCnt.getAndIncrement();
                    study.setReplicateSampleDescription(studySampleDescPatchRequest.getReplicateSampleDescription());
                    sampleDescChanged = true;
                    sampleChangesBuilder.append("Replicated Sample Description changed successfully for GCST "+study.getAccession() + " and Study Tag "+study.getStudyTag());
                    sampleChangesBuilder.append("\n");
                }
                if (sampleDescChanged)
                    studyRepository.save(study);

            } else {
                if(invalidStudyTag){
                    if(studySampleDescPatchRequest.getInitialSampleDescription() != null && !study.getInitialSampleDescription().equalsIgnoreCase(studySampleDescPatchRequest.getInitialSampleDescription())) {
                        sampleChangesBuilder.append("Initial Sample Description changes failed for GCST " + study.getAccession() + " as Study Tag did not match " + study.getStudyTag());
                        sampleChangesBuilder.append("\n");
                    }
                    if(studySampleDescPatchRequest.getReplicateSampleDescription() != null && !study.getReplicateSampleDescription().equalsIgnoreCase(studySampleDescPatchRequest.getReplicateSampleDescription())) {
                        sampleChangesBuilder.append("Initial Sample Description changes failed for GCST " + study.getAccession() + " as Study Tag did not match " + study.getStudyTag());
                        sampleChangesBuilder.append("\n");
                    }
                }
                else{
                    sampleChangesBuilder.append("Sample Description changes failed for missing GCST "+ study.getAccession() + " and Study Tag  " + study.getStudyTag());
                    sampleChangesBuilder.append("\n");
                }
            }

        });


        String replicatedSampleDescText = "Number of Replication Description changed is -: "+ String.valueOf(replicatedSampleDescCnt);
        String initialSampleDescText = "Number of Initial Description changed is -: "+String.valueOf(initialSampleDescCnt);
        finalUploadBuilder.append(initialSampleDescText);
        finalUploadBuilder.append("\n");
        finalUploadBuilder.append(replicatedSampleDescText);
        finalUploadBuilder.append("\n");
        finalUploadBuilder.append(sampleChangesBuilder);
        return  finalUploadBuilder.toString().getBytes();


    }

    public void sendStudyChangeMessage(Study study){
        studyIngestPublisher.send(studyRabbitMessageAssembler.assemble(study));
    }

    public Stream<Study> getStudies(List<String> ids) {
        return studyRepository.readByIdIn(ids);
    }

    public String diffDiseaseTrait(String submissionId, String studyTag, String oldDiseaseTraitId, String newDiseaseTraitId) {
      Optional<DiseaseTrait> optOldDiseaseTrait =  diseaseTraitService.getDiseaseTrait(oldDiseaseTraitId);
      DiseaseTraitDto oldDiseaseTraitDto = optOldDiseaseTrait.isPresent() ?
              diseaseTraitDtoAssembler.assemble(optOldDiseaseTrait.get())
                : null;
      Optional<DiseaseTrait> optNewDiseaseTrait =  diseaseTraitService.getDiseaseTrait(newDiseaseTraitId);
      DiseaseTraitDto newDiseaseTraitDto = optNewDiseaseTrait.isPresent() ?
                diseaseTraitDtoAssembler.assemble(optNewDiseaseTrait.get())
                : null;
      return String.format("Submission Id %s Study Tag %s update from  %s to %s", submissionId, studyTag, oldDiseaseTraitDto.getTrait(), newDiseaseTraitDto.getTrait());
    }

    public String diffEFOTrait(String submissionId, String studyTag, List<String> oldEFOTraitIds, List<String> newEFOTraitIds) {

      String oldEFOTraits =  oldEFOTraitIds.stream().map(id -> efoTraitService.getEfoTrait(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(EfoTrait::getTrait)
                .collect(Collectors.joining("|"));

        String newEFOTraits =  newEFOTraitIds.stream().map(id -> efoTraitService.getEfoTrait(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(EfoTrait::getTrait)
                .collect(Collectors.joining("|"));


        return String.format("Submission Id %s Study Tag %s update from  %s to %s", submissionId, studyTag, oldEFOTraits, newEFOTraits);
    }

}
