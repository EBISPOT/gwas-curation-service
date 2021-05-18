package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;


@Component
public class StudyDtoAssembler {


    public static StudyDto assemble(Study study) {
        return new StudyDto(study.getStudyTag(),
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
                study.isAgreedToCc0());
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
}
