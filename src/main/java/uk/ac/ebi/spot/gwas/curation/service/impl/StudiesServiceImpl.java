package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<TraitUploadReport> updateTraitsForStudies(List<StudyPatchRequest> studyPatchRequests, String submissionId) {
        List<TraitUploadReport> report = new ArrayList<>();
        studyPatchRequests.forEach((studyPatchRequest) -> {
            boolean invalidStudyTag = false;
            Study study = getStudyByAccession(studyPatchRequest.getGcst().trim(), submissionId);
            if(!study.getStudyTag().equalsIgnoreCase(studyPatchRequest.getStudyTag().trim())){
                invalidStudyTag = true;
            }
            Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitService.getDiseaseTraitByTraitName(studyPatchRequest.getCuratedReportedTrait().trim());
            if(study != null && !invalidStudyTag) {
                if (optionalDiseaseTrait.isPresent()) {
                    DiseaseTrait diseaseTrait = optionalDiseaseTrait.get();

                    study.setDiseaseTrait(diseaseTrait.getId());
                    studyRepository.save(study);
                    report.add(new TraitUploadReport(diseaseTrait.getTrait(), "Study for accession " + studyPatchRequest.getGcst() + " successfully Updated with trait : " + studyPatchRequest.getCuratedReportedTrait(), studyPatchRequest.getGcst()));
                }else {
                    report.add(new TraitUploadReport(studyPatchRequest.getCuratedReportedTrait(), "Study for accession " + studyPatchRequest.getGcst() + " failed with trait : " + studyPatchRequest.getCuratedReportedTrait()+" not present in DB", studyPatchRequest.getGcst()));
                }
            } else {
                if(invalidStudyTag)
                    report.add(new TraitUploadReport(studyPatchRequest.getCuratedReportedTrait(), "Study for accession " + studyPatchRequest.getGcst() + " with trait : " + studyPatchRequest.getCuratedReportedTrait()+" failed as study tag is not matched with DB entry "+study.getStudyTag(), studyPatchRequest.getGcst()));
                else
                report.add(new TraitUploadReport(studyPatchRequest.getCuratedReportedTrait(), "Study for accession " + studyPatchRequest.getGcst() + " with trait : " + studyPatchRequest.getCuratedReportedTrait()+" failed as study not present in DB", studyPatchRequest.getGcst()));
            }
        });
        return report;
    }

    @Override
    public List<TraitUploadReport> updateEfoTraitsForStudies(List<EfoTraitStudyMappingDto> efoTraitStudyMappingDtos, String submissionId) {

        List<TraitUploadReport> report = new ArrayList<>();
        efoTraitStudyMappingDtos.forEach((efoTraitStudyMappingDto -> {
            boolean invalidStudyTag = false;
            Study study = getStudyByAccession(efoTraitStudyMappingDto.getGcst().trim(), submissionId);
            if(!efoTraitStudyMappingDto.getStudyTag().trim().equalsIgnoreCase(study.getStudyTag()))
                invalidStudyTag = true;
            Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findByShortForm(efoTraitStudyMappingDto.getShortForm().trim());
            if(study != null && !invalidStudyTag) {
                if (efoTraitOptional.isPresent()) {
                    EfoTrait efoTrait = efoTraitOptional.get();
                    List<String> traitsList = study.getEfoTraits();
                    if (traitsList == null) {
                        traitsList = new ArrayList<>();
                    }
                    if (!traitsList.contains(efoTrait.getId())) {
                        traitsList.add(efoTrait.getId());
                    }
                    study.setEfoTraits(traitsList);
                    studyRepository.save(study);
                    report.add(new TraitUploadReport(efoTraitStudyMappingDto.getShortForm(), "Study for accession " + efoTraitStudyMappingDto.getGcst() + " successfully updated with trait : " + efoTraitStudyMappingDto.getShortForm(), efoTraitStudyMappingDto.getGcst()));
                } else {
                    report.add(new TraitUploadReport(efoTraitStudyMappingDto.getShortForm(), "Study for accession " + efoTraitStudyMappingDto.getGcst() + " failed as trait : " + efoTraitStudyMappingDto.getShortForm()+" not present in DB", efoTraitStudyMappingDto.getGcst()));
                }
            } else {
                if(invalidStudyTag)
                    report.add(new TraitUploadReport(efoTraitStudyMappingDto.getShortForm(), "Study for accession " + efoTraitStudyMappingDto.getGcst() + " with trait : " + efoTraitStudyMappingDto.getShortForm()+" failed as study tag is not matched with DB entry "+study.getStudyTag(), efoTraitStudyMappingDto.getGcst()));
                else
                report.add(new TraitUploadReport(efoTraitStudyMappingDto.getShortForm(), "Study for accession " + efoTraitStudyMappingDto.getGcst() + " with trait : " + efoTraitStudyMappingDto.getShortForm()+" failed as study not present in DB", efoTraitStudyMappingDto.getGcst()));
            }
        }));
        return report;
    }

    @Override
    public UploadReportWrapper updateMultiTraitsForStudies(List<MultiTraitStudyMappingDto> multiTraitStudyMappingDtos, String submissionId) {

        List<MultiTraitStudyMappingReport> report = new ArrayList<>();
        UploadReportWrapper uploadReportWrapper = new UploadReportWrapper();
        multiTraitStudyMappingDtos.forEach(multiTraitStudyMappingDto -> {
            boolean invalidStudyTag = false;
            Study study = getStudyByAccession(multiTraitStudyMappingDto.getGcst().trim(), submissionId);
            if (study == null) {
                uploadReportWrapper.setHasErrors(true);
                report.add(new MultiTraitStudyMappingReport(multiTraitStudyMappingDto.getGcst(), multiTraitStudyMappingDto.getStudyTag(), "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag."));
            }
            else {
                if(!multiTraitStudyMappingDto.getStudyTag().trim().equalsIgnoreCase(study.getStudyTag())) {
                    invalidStudyTag = true;
                }
                 String efoTraitComments = "";
                String bgEfoTraitComments = "";
                if (!invalidStudyTag) {
                    // --- EFO Traits ---

                    // Get already existing EFOs for study
                    HashSet<String> oldEfos = new HashSet<>();
                    List<String> oldEfosForReport = new ArrayList<>();
                    if (study.getEfoTraits() != null) {
                        for (String efoId: study.getEfoTraits()) {
                            Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findById(efoId);
                            efoTraitOptional.ifPresent(efoTrait -> oldEfos.add(efoTrait.getShortForm()));
                        }
                        oldEfosForReport = new ArrayList<>(oldEfos);
                    }
                    // Get pipe separated short forms from template
                    HashSet<String> newEfos = new HashSet<>(Arrays.asList(StringUtils.deleteWhitespace(multiTraitStudyMappingDto.getEfoTraitShortForm().trim()).split("\\|")));
                    ArrayList<String> addedEfos = new ArrayList<>();
                    // oldEfos that arent in newEfos will be added to deletedEfos
                    // newEfos that arent in oldEfos will be added to addedEfos
                    ArrayList<String> studyEfoTraitsIds = new ArrayList<>();
                    ArrayList<String> studyEfoTraitsShortForms = new ArrayList<>();
                    for (String shortForm : newEfos) {
                        Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findByShortForm(shortForm.trim());
                        if (efoTraitOptional.isPresent()) {
                            studyEfoTraitsIds.add(efoTraitOptional.get().getId());
                            studyEfoTraitsShortForms.add(efoTraitOptional.get().getShortForm());
                            if (!oldEfos.contains(shortForm)) {
                                addedEfos.add(shortForm);
                            }
                            else {
                                oldEfos.remove(shortForm);
                            }
                        } else {
                            uploadReportWrapper.setHasErrors(true);
                            efoTraitComments = efoTraitComments.concat("\n" + shortForm + " not found in DB.");
                        }
                    }
                    study.setEfoTraits(studyEfoTraitsIds);

                    ArrayList<String> removedEfos = new ArrayList<>(oldEfos);
                    efoTraitComments = efoTraitComments.concat("\nCurrent: " + StringUtils.join(studyEfoTraitsShortForms, "|"));
                    efoTraitComments = efoTraitComments.concat("\nOld: " + StringUtils.join(oldEfosForReport, "|"));
                    efoTraitComments = efoTraitComments.concat("\nAdded: " + StringUtils.join(addedEfos, "|"));
                    efoTraitComments = efoTraitComments.concat("\nRemoved: " + StringUtils.join(removedEfos, "|"));

                    // --- Background EFO traits

                    // Get already existing EFOs for study
                    HashSet<String> oldBgEfos = new HashSet<>();
                    List<String> oldBgEfosForReport = new ArrayList<>();
                    if (study.getBackgroundEfoTraits() != null) {
                        for (String efoId: study.getBackgroundEfoTraits()) {
                            Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findById(efoId);
                            efoTraitOptional.ifPresent(efoTrait -> oldBgEfos.add(efoTrait.getShortForm()));
                        }
                        oldBgEfosForReport = new ArrayList<>(oldBgEfos);
                    }
                    HashSet<String> newBgEfos = new HashSet<>(Arrays.asList(StringUtils.deleteWhitespace(multiTraitStudyMappingDto.getBackgroundEfoShortForm().trim()).split("\\|")));
                    ArrayList<String> addedBgEfos = new ArrayList<>();
                    ArrayList<String> studyBgEfoTraitsIds = new ArrayList<>();
                    ArrayList<String> studyBgEfoTraitsShortForms = new ArrayList<>();
                    for (String shortForm : newBgEfos) {
                        Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findByShortForm(shortForm.trim());
                        if (efoTraitOptional.isPresent()) {
                            studyBgEfoTraitsIds.add(efoTraitOptional.get().getId());
                            studyBgEfoTraitsShortForms.add(efoTraitOptional.get().getShortForm());
                            if (!oldBgEfos.contains(shortForm)) {
                                addedBgEfos.add(shortForm);
                            }
                            else {
                                oldBgEfos.remove(shortForm);
                            }
                        } else {
                            uploadReportWrapper.setHasErrors(true);
                            bgEfoTraitComments = bgEfoTraitComments.concat("\n" + shortForm + " not found in DB.");
                        }
                    }
                    study.setBackgroundEfoTraits(studyBgEfoTraitsIds);

                    ArrayList<String> removedBgEfos = new ArrayList<>(oldBgEfos);
                    bgEfoTraitComments = bgEfoTraitComments.concat("\nCurrent: " + StringUtils.join(studyBgEfoTraitsShortForms, "|"));
                    bgEfoTraitComments = bgEfoTraitComments.concat("\nOld: " + StringUtils.join(oldBgEfosForReport, "|"));
                    bgEfoTraitComments = bgEfoTraitComments.concat("\nAdded: " + StringUtils.join(addedBgEfos, "|"));
                    bgEfoTraitComments = bgEfoTraitComments.concat("\nRemoved: " + StringUtils.join(removedBgEfos, "|"));

                    studyRepository.save(study);

                    Optional<DiseaseTrait> diseaseTraitOptional = diseaseTraitService.getDiseaseTraitByTraitName(multiTraitStudyMappingDto.getReportedTrait().trim());
                    String reportedTraitComments = "";
                    if (diseaseTraitOptional.isPresent()) {
                        study.setDiseaseTrait(diseaseTraitOptional.get().getId());
                        studyRepository.save(study);
                        reportedTraitComments = reportedTraitComments.concat("Reported trait set to: " + diseaseTraitOptional.get().getTrait());
                    } else {
                        uploadReportWrapper.setHasErrors(true);
                        reportedTraitComments = reportedTraitComments.concat("Reported trait " + multiTraitStudyMappingDto.getReportedTrait() + " not found in DB");
                    }
                    report.add(new MultiTraitStudyMappingReport(study.getAccession(), study.getStudyTag(), efoTraitComments.trim(), bgEfoTraitComments.trim(), reportedTraitComments.trim()));
                }
                else {
                    uploadReportWrapper.setHasErrors(true);
                    report.add(new MultiTraitStudyMappingReport(multiTraitStudyMappingDto.getGcst(), multiTraitStudyMappingDto.getStudyTag(), "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag.", "Study not found. Please check accession and tag."));
                }
            }
        });
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
        studySampleDescPatchRequests.forEach((studySampleDescPatchRequest) ->
        {
            boolean invalidStudyTag = false;
            boolean sampleDescChanged = false;
            Study study = getStudyByAccession(studySampleDescPatchRequest.getGcst(), submissionId);
            if(!studySampleDescPatchRequest.getStudyTag().equalsIgnoreCase(study.getStudyTag()))
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
