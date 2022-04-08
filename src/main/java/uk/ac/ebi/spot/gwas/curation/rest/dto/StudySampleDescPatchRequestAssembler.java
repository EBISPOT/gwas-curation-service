package uk.ac.ebi.spot.gwas.curation.rest.dto;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.StudiesController;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudySampleDescPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudySampleDescPatchWrapper;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Component
public class StudySampleDescPatchRequestAssembler implements ResourceAssembler<Study, Resource<StudySampleDescPatchRequest>> {

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Autowired
    StudiesService studiesService;

    @Override
    public Resource<StudySampleDescPatchRequest> toResource(Study study) {

        StudySampleDescPatchRequest studySampleDescPatchRequest = StudySampleDescPatchRequest.builder().studyTag(study.getStudyTag())
                                                .initialSampleDescription(study.getInitialSampleDescription())
                                                .replicateSampleDescription(study.getReplicateSampleDescription())
                                                .gcst(study.getAccession()).build();

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(StudiesController.class).getStudy(study.getId(), study.getSubmissionId()));

        Resource<StudySampleDescPatchRequest> resource = new Resource<>(studySampleDescPatchRequest);
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;

    }

    public StudySampleDescPatchRequest assemble(Study study){
        StudySampleDescPatchRequest studySampleDescPatchRequest = new StudySampleDescPatchRequest();
        studySampleDescPatchRequest.setGcst(study.getAccession());
        studySampleDescPatchRequest.setInitialSampleDescription(study.getInitialSampleDescription());
        studySampleDescPatchRequest.setReplicateSampleDescription(study.getReplicateSampleDescription());
        studySampleDescPatchRequest.setStudyTag(study.getStudyTag());
        return studySampleDescPatchRequest;
    }


    public Study disassemble(StudySampleDescPatchRequest studySampleDescPatchRequest, String studyId){
        Study study = studiesService.getStudy(studyId);
        Optional.ofNullable(studySampleDescPatchRequest.getInitialSampleDescription()).ifPresent(initialSample -> study.setInitialSampleDescription(initialSample.trim()));
        Optional.ofNullable(studySampleDescPatchRequest.getReplicateSampleDescription()).ifPresent(replicateSample -> study.setReplicateSampleDescription(replicateSample.trim()));
        return study;
    }


    public StudySampleDescPatchWrapper assembleWrapper(Study study) {
        StudySampleDescPatchWrapper studySampleDescPatchWrapper = StudySampleDescPatchWrapper.builder()
                                                                .gcst(study.getAccession())
                                                                .studyTag(study.getStudyTag())
                                                                .initialSampleDescription(study.getInitialSampleDescription())
                                                                .replicateSampleDescription(study.getReplicateSampleDescription())
                                                                .build();

        return studySampleDescPatchWrapper;
    }

    public List<StudyPatchRequest> disassemble(MultipartFile multipartFile) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema csvSchema = FileHandler.getSchemaFromMultiPartFile(multipartFile);
        List<StudyPatchRequest> studyPatchRequestList;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            MappingIterator<StudyPatchRequest> iterator = mapper.readerFor(StudySampleDescPatchRequest.class).with(csvSchema).readValues(inputStream);
            studyPatchRequestList = iterator.readAll();
        }catch (IOException ex){
            throw new FileProcessingException("Could not read the file");
        }
        return studyPatchRequestList;
    }

}
