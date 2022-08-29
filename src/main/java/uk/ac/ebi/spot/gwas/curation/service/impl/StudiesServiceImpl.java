package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudySampleDescPatchRequestAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class StudiesServiceImpl implements StudiesService {

    private static final Logger log = LoggerFactory.getLogger(StudiesService.class);

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private DiseaseTraitService diseaseTraitService;

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

    @Override
    public Study updateStudies(Study study) {
        log.info("Inside updateStudies");
        return studyRepository.save(study);
    }

    @Override
    public Study getStudy(String studyId) {
        log.info("Retrieving study: {}", studyId);
        Optional<Study> studyOptional = studyRepository.findById(studyId);
        if (studyOptional.isPresent()) {
            log.info("Found study: {}", studyOptional.get().getStudyTag());
            return studyOptional.get();
        }
        log.error("Unable to find study: {}", studyId);
        return null;
    }



    @Override
    public Page<Study> getStudies(String submissionId,  Pageable page) {
        return studyRepository.findBySubmissionId(submissionId, page);
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
                    invalidStudyTag = true;
                }

                if (!invalidStudyTag) {
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
                        DiseaseTrait diseaseTrait = retrievedReportedTraits.get(multiTraitStudyMappingDto.getReportedTrait().trim());
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
            Query query = new Query().addCriteria(new Criteria("id").is(study.getId()));
            Update update = new Update()
                    .set("efoTraits", study.getEfoTraits())
                    .set("backgroundEfoTraits", study.getBackgroundEfoTraits())
                    .set("diseaseTrait", study.getDiseaseTrait());
            bulkOps.updateOne(query, update);
        }
        bulkOps.execute();
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
}
