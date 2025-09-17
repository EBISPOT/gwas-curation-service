package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MultiTraitStudyMappingDto;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MultiTraitStudyMappingDtoAssembler {

    @Autowired
    EfoTraitService efoTraitService;

    @Autowired
    DiseaseTraitService diseaseTraitService;

  public  MultiTraitStudyMappingDto assemble(Study study) {
      String efoShortForms = study.getEfoTraits() != null ?
              study.getEfoTraits()
              .stream()
              .map(efoId -> efoTraitService.getEfoTrait(efoId))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .map(EfoTrait::getShortForm)
              .collect(Collectors.joining("\\| ")) : "";

      String bgEfoShortForms = study.getBackgroundEfoTraits() != null ?
              study.getBackgroundEfoTraits()
                      .stream()
              .map(efoId -> efoTraitService.getEfoTrait(efoId))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .map(EfoTrait::getShortForm)
              .collect(Collectors.joining("\\| ")) : "";

      String reportedTrait = Optional.ofNullable(study.getDiseaseTrait())
              .map(traitId -> diseaseTraitService.getDiseaseTrait(traitId))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .map(DiseaseTrait::getTrait)
              .orElse("");


      return MultiTraitStudyMappingDto.builder()
              .gcst(study.getAccession())
              .studyTag(study.getStudyTag())
              .efoTraitShortForm(efoShortForms)
              .backgroundEfoShortForm(bgEfoShortForms)
              .reportedTrait(reportedTrait)
              .build();


  }

}
