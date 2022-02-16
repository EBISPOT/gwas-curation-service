package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.DiseaseTraitController;
import uk.ac.ebi.spot.gwas.curation.rest.StudiesController;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@Component
public class StudyDtoAssembler implements ResourceAssembler<Study, Resource<StudyDto>> {

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    EfoTraitService efoTraitService;

    @Autowired
    EfoTraitDtoAssembler efoTraitDtoAssembler;

    @Autowired
    StudiesService studiesService;

    @Override
    public Resource<StudyDto>  toResource(Study study) {

        List<String> traitsList = null;
        List<DiseaseTrait> traits = null;
        if(study.getDiseaseTraits() != null && !study.getDiseaseTraits().isEmpty() ){
            traitsList = study.getDiseaseTraits();
        }
        if( traitsList != null && !traitsList.isEmpty() )
         traits =  study.getDiseaseTraits().stream().map((traitId) ->
                        diseaseTraitService.getDiseaseTrait(traitId))
                        .filter(optDiseaseTrait -> optDiseaseTrait.isPresent())
                        .map(optDiseaseTrait -> optDiseaseTrait.get())
                        .collect(Collectors.toList());

        List<EfoTrait> efoTraits = null;
        if(study.getEfoTraits() != null && !study.getEfoTraits().isEmpty() ){
            efoTraits = study.getEfoTraits().stream().map((traitId) ->
                    efoTraitService.getEfoTrait(traitId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());;
        }

        StudyDto studyDto = StudyDto.builder().
                studyTag(study.getStudyDescription())
                .studyId(study.getId())
                .studyDescription(study.getStudyDescription())
                .accession(study.getAccession())
                .diseaseTraits(DiseaseTraitDtoAssembler.assemble(traits))
                .efoTraits(EfoTraitDtoAssembler.assemble(efoTraits))
                .arrayInformation(study.getArrayManufacturer())
                .efoTrait(study.getEfoTrait())
                .arrayManufacturer(study.getArrayManufacturer())
                .arrayInformation(study.getArrayInformation())
                .checksum(study.getChecksum())
                .genotypingTechnology(study.getGenotypingTechnology())
                .cohort(study.getCohort())
                .variantCount(study.getVariantCount())
                .build();

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(StudiesController.class).getStudy(study.getId(), study.getSubmissionId()));

        Resource<StudyDto> resource = new Resource<>(studyDto);
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));

        if(traits != null && !traits.isEmpty()) {
            ControllerLinkBuilder lb1 = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(StudiesController.class).getDiseaseTraits(null, study.getId(), study.getSubmissionId()));

            Link diseaseTraitsLink = BackendUtil.underBasePath(lb1, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_DISEASE_TRAITS);
            resource.add(diseaseTraitsLink);
        }

        return resource;

    }



    public static StudyDto assemble(Study study) {
        return new StudyDto(study.getStudyTag(),
                study.getId(),
                study.getAccession(),
                study.getGenotypingTechnology(),
                study.getArrayManufacturer(),
                study.getArrayInformation(),
                study.getImputation(),
                study.getVariantCount(),
                study.getSampleDescription(),
                study.getStatisticalModel(),
                study.getStudyDescription(),
                study.getTrait(),
                study.getEfoTrait(),
                study.getBackgroundTrait(),
                study.getBackgroundEfoTrait(),
                study.getSummaryStatisticsFile(),
                study.getRawFilePath(),
                study.getChecksum(),
                study.getSummaryStatisticsAssembly(),
                study.getReadmeFile(),
                study.getCohort(),
                study.getCohortId(),
                null,
                null,
                null,
                study.isAgreedToCc0(),
                null,
                null);
    }

    public static Study disassemble(StudyDto studyDto) {
        Study study = new Study();
        study.setStudyTag(studyDto.getStudyTag());
        study.setAccession(studyDto.getAccession());
        study.setGenotypingTechnology(studyDto.getGenotypingTechnology());
        study.setArrayManufacturer(studyDto.getArrayManufacturer());
        study.setArrayInformation(studyDto.getArrayInformation());
        study.setImputation(studyDto.getImputation());
        study.setVariantCount(studyDto.getVariantCount());
        study.setStatisticalModel(studyDto.getStatisticalModel());
        study.setStudyDescription(studyDto.getStudyDescription());
        study.setTrait(studyDto.getTrait());
        study.setSampleDescription(studyDto.getSampleDescription());
        study.setEfoTrait(studyDto.getEfoTrait());
        study.setBackgroundEfoTrait(studyDto.getBackgroundEfoTrait());
        study.setBackgroundTrait(studyDto.getBackgroundTrait());
        study.setSummaryStatisticsAssembly(studyDto.getSummaryStatisticsAssembly());
        study.setSummaryStatisticsFile(studyDto.getSummaryStatisticsFile());
        study.setRawFilePath(studyDto.getRawSumstatsFile());
        study.setReadmeFile(studyDto.getReadmeFile());
        study.setChecksum(studyDto.getChecksum());
        study.setCohort(studyDto.getCohort());
        study.setCohortId(studyDto.getCohortId());

        return study;
    }

    public  Study disassembleForExsitingStudy(StudyDto studyDto, String studyId) {
        Study study = studiesService.getStudy(studyId);
        Optional.ofNullable(studyDto.getStudyTag()).ifPresent(tag -> study.setStudyTag(tag));
        Optional.ofNullable(studyDto.getGenotypingTechnology()).ifPresent(genoType -> study.setGenotypingTechnology(genoType));
        Optional.ofNullable(studyDto.getArrayManufacturer()).ifPresent(arrayManu -> study.setArrayManufacturer(arrayManu));
        Optional.ofNullable(studyDto.getArrayInformation()).ifPresent(arrayInfo -> study.setArrayInformation(arrayInfo));
        Optional.ofNullable(studyDto.getImputation()).ifPresent(imputation -> study.setImputation(imputation));
        Optional.ofNullable(studyDto.getVariantCount()).ifPresent(varCount -> study.setVariantCount(varCount));
        Optional.ofNullable(studyDto.getStatisticalModel()).ifPresent(statModel -> study.setStatisticalModel(statModel));
        Optional.ofNullable(studyDto.getStudyDescription()).ifPresent(studyDesc -> study.setStudyDescription(studyDesc));
        Optional.ofNullable(studyDto.getTrait()).ifPresent(trait -> study.setTrait(trait));
        Optional.ofNullable(studyDto.getSampleDescription()).ifPresent(sampleDesc -> study.setSampleDescription(sampleDesc));
        Optional.ofNullable(studyDto.getEfoTrait()).ifPresent(efoTrait -> study.setEfoTrait(efoTrait));
        Optional.ofNullable(studyDto.getBackgroundEfoTrait()).ifPresent(backgroundTrait -> study.setBackgroundEfoTrait(backgroundTrait));
        Optional.ofNullable(studyDto.getBackgroundTrait()).ifPresent(backgroundTrait -> study.setBackgroundTrait(backgroundTrait));
        Optional.ofNullable(studyDto.getSummaryStatisticsAssembly()).ifPresent(sumStatsAssembly -> study.setSummaryStatisticsAssembly(sumStatsAssembly));
        Optional.ofNullable(studyDto.getSummaryStatisticsFile()).ifPresent(sumStatsFile -> study.setSummaryStatisticsFile(sumStatsFile));
        Optional.ofNullable(studyDto.getRawSumstatsFile()).ifPresent(rawSumStatsFile -> study.setRawFilePath(rawSumStatsFile));
        Optional.ofNullable(studyDto.getReadmeFile()).ifPresent(readMeFile -> study.setReadmeFile(readMeFile));
        Optional.ofNullable(studyDto.getChecksum()).ifPresent(checkSum -> study.setChecksum(checkSum));
        Optional.ofNullable(studyDto.getCohort()).ifPresent(cohort -> study.setCohort(cohort));
        Optional.ofNullable(studyDto.getCohortId()).ifPresent(cohortId -> study.setCohort(cohortId));

        return study;
    }
}
