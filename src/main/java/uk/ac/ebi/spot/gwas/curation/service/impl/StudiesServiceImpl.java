package uk.ac.ebi.spot.gwas.curation.service.impl;

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
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            Study study = getStudyByAccession(studyPatchRequest.getGcst(), submissionId);
            Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitService.getDiseaseTraitByTraitName(studyPatchRequest.getCuratedReportedTrait());
            if(study != null) {
                if (optionalDiseaseTrait.isPresent()) {
                    DiseaseTrait diseaseTrait = optionalDiseaseTrait.get();

                    study.setDiseaseTrait(diseaseTrait.getId());
                    studyRepository.save(study);
                    report.add(new TraitUploadReport(diseaseTrait.getTrait(), "Study for accession " + studyPatchRequest.getGcst() + " successfully Updated with trait : " + studyPatchRequest.getCuratedReportedTrait(), studyPatchRequest.getGcst()));
                }else {
                    report.add(new TraitUploadReport(studyPatchRequest.getCuratedReportedTrait(), "Study for accession " + studyPatchRequest.getGcst() + " failed with trait : " + studyPatchRequest.getCuratedReportedTrait()+" not present in DB", studyPatchRequest.getGcst()));
                }
            } else {
                report.add(new TraitUploadReport(studyPatchRequest.getCuratedReportedTrait(), "Study for accession " + studyPatchRequest.getGcst() + " with trait : " + studyPatchRequest.getCuratedReportedTrait()+" failed as study not present in DB", studyPatchRequest.getGcst()));
            }
        });
        return report;
    }

    @Override
    public List<TraitUploadReport> updateEfoTraitsForStudies(List<EfoTraitStudyMappingDto> efoTraitStudyMappingDtos, String submissionId) {

        List<TraitUploadReport> report = new ArrayList<>();
        efoTraitStudyMappingDtos.forEach((efoTraitStudyMappingDto -> {
            Study study = getStudyByAccession(efoTraitStudyMappingDto.getGcst(), submissionId);
            Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findByShortForm(efoTraitStudyMappingDto.getShortForm());
            if(study != null) {
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
                report.add(new TraitUploadReport(efoTraitStudyMappingDto.getShortForm(), "Study for accession " + efoTraitStudyMappingDto.getGcst() + " with trait : " + efoTraitStudyMappingDto.getShortForm()+" failed as study not present in DB", efoTraitStudyMappingDto.getGcst()));
            }
        }));
        return report;
    }


    public List<StudySampleDescPatchRequest> updateSampleDescription(List<StudySampleDescPatchRequest> studySampleDescPatchRequests, String submissionId) {
        return studySampleDescPatchRequests.stream().map((studySampleDescPatchRequest) ->
                     Optional.ofNullable(getStudyByAccession(studySampleDescPatchRequest.getGcst(), submissionId))
                            .map(study -> studySampleDescPatchRequestAssembler.disassemble(studySampleDescPatchRequest, study.getId()))
                            .map(this::updateStudies)
                            .map(studySampleDescPatchRequestAssembler::assemble).orElse(null)
                ).collect(Collectors.toList());

    }
}
