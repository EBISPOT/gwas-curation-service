package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.StudiesController;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudySampleDescPatchRequest;

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
        Optional.ofNullable(studySampleDescPatchRequest.getInitialSampleDescription()).ifPresent(initialSample -> study.setInitialSampleDescription(initialSample));
        Optional.ofNullable(studySampleDescPatchRequest.getReplicateSampleDescription()).ifPresent(replicateSample -> study.setReplicateSampleDescription(replicateSample));
        return study;
    }
}
