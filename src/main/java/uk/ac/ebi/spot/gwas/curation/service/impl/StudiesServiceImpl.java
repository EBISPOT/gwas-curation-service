package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;

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
    private DiseaseTraitRepository diseaseTraitRepository;

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
    public List<String> getTraitsIDsFromDB(List<DiseaseTraitDto> diseaseTraitDtos, String studyId) {
       List<String> newTraitIds = diseaseTraitDtos.stream().map(diseaseTraitDto ->
                diseaseTraitService.getDiseaseTraitByTraitName(diseaseTraitDto.getTrait()))
                .filter((optTrait) -> optTrait.isPresent())
                .map((optTrait) -> optTrait.get().getId() )
                .collect(Collectors.toList());

        Study study = getStudy(studyId);
        List<String> origTraitIds = study.getDiseaseTraits();

        newTraitIds.forEach((traitId) -> {
            if (!origTraitIds.contains(traitId)) {
                origTraitIds.add(traitId);
            }
        });

        return origTraitIds;
    }

    @Override
    public Page<Study> getStudies(String submissionId,  Pageable page) {
        return studyRepository.findBySubmissionId(submissionId, page);
    }

    public List<DiseaseTrait> getDiseaseTraitsByStudyId(String studyId) {
      return   getStudy(studyId).getDiseaseTraits().stream()
                .map((traitId) -> diseaseTraitRepository.findById(traitId) )
                .filter((optDiseaseTrait) -> optDiseaseTrait.isPresent())
                .map((optDiseaseTrait) -> optDiseaseTrait.get())
                .collect(Collectors.toList());
    }

    @Override
    public Study getStudyByAccession(String accessionId) {
        log.info("Retrieving study from accession: {}", accessionId);
        Optional<Study> studyOptional = studyRepository.findByAccession(accessionId);
        if (studyOptional.isPresent()) {
            log.info("Found study: {}", studyOptional.get().getStudyTag());
            return studyOptional.get();
        }
        log.error("Unable to find study with : {}", accessionId);
        return null;
    }

    @Override
    public List<TraitUploadReport> updateTraitsForStudies(List<StudyPatchRequest> studyPatchRequests) {
        List<TraitUploadReport> report = new ArrayList<>();
        studyPatchRequests.forEach((studyPatchRequest) -> {
            Study study = getStudyByAccession(studyPatchRequest.getGcst());
            Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitService.getDiseaseTraitByTraitName(studyPatchRequest.getCuratedReportedTrait());
            if(study != null) {
                if (optionalDiseaseTrait.isPresent()) {
                    DiseaseTrait diseaseTrait = optionalDiseaseTrait.get();

                    List<String> traitsList = study.getDiseaseTraits();
                    if (traitsList != null) {
                    }
                    else {
                        traitsList = new ArrayList<>();
                    }
                    if (!traitsList.contains(diseaseTrait.getId())) {
                        traitsList.add(diseaseTrait.getId());
                    }
                    study.setDiseaseTraits(traitsList);
                    studyRepository.save(study);
                    report.add(new TraitUploadReport(diseaseTrait.getTrait(), "Study for accession" + studyPatchRequest.getGcst() + " successfully Updated with trait : " + studyPatchRequest.getCuratedReportedTrait(), studyPatchRequest.getGcst()));
                }else {
                    report.add(new TraitUploadReport(studyPatchRequest.getCuratedReportedTrait(), "Study for accession" + studyPatchRequest.getGcst() + " failed with trait : " + studyPatchRequest.getCuratedReportedTrait()+" not present in DB", studyPatchRequest.getGcst()));
                }
            } else {
                report.add(new TraitUploadReport(studyPatchRequest.getCuratedReportedTrait(), "Study for accession" + studyPatchRequest.getGcst() + " with trait : " + studyPatchRequest.getCuratedReportedTrait()+" failed as study not present in DB", studyPatchRequest.getGcst()));
            }
        });
        return report;
    }

}
