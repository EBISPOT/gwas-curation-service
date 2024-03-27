package uk.ac.ebi.spot.gwas.curation.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rabbitmq.MetadataYmlUpdatePublisher;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudySampleDescPatchRequestAssembler;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class StudiesController {

    private static final Logger log = LoggerFactory.getLogger(StudiesController.class);

    @Autowired
    StudiesService studiesService;

    @Autowired
    StudyDtoAssembler studyDtoAssembler;

    @Autowired
    StudySampleDescPatchRequestAssembler studySampleDescPatchRequestAssembler;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}"+DepositionCurationConstants.API_STUDIES+"/{studyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<StudyDto> getStudy(@PathVariable String studyId, @PathVariable String submissionId) {
        Study study = studiesService.getStudy(studyId);
        if( study != null ) {
            return studyDtoAssembler.toResource(study);
        } else {
            throw new EntityNotFoundException("Study not found "+ studyId);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}"+DepositionCurationConstants.API_STUDIES+"/{studyId}/diseasetraits", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<DiseaseTraitDto> getDiseaseTraits(PagedResourcesAssembler assembler, @PathVariable String studyId, @PathVariable String submissionId) {

        DiseaseTrait diseaseTrait = studiesService.getDiseaseTraitsByStudyId(studyId);
        List<Resource<DiseaseTraitDto>> resourcesList = new ArrayList<>();

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(StudiesController.class).getDiseaseTraits(assembler, studyId, submissionId));

            DiseaseTraitDto diseaseTraitDto = diseaseTraitDtoAssembler.assemble(diseaseTrait);
            Resource<DiseaseTraitDto> resource = new Resource<>(diseaseTraitDto);
            ControllerLinkBuilder lb1 = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(DiseaseTraitController.class).getDiseaseTrait(diseaseTrait.getId()));
            resource.add(BackendUtil.underBasePath(lb1, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));


        return resource;
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{submissionId}"+DepositionCurationConstants.API_STUDIES+"/{studyId}",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public  Resource<StudyDto> updateStudies(@PathVariable String studyId, @PathVariable String submissionId, @Valid @RequestBody StudyDto studyDto, HttpServletRequest request) {
        List<String> traitIds = null;
        List<String> efoTraitIds = null;
        if(studiesService.getStudy(studyId) != null ) {
            log.info("Disease Traits from request:" + studyDto.getDiseaseTrait());
            /*if (studyDto.getDiseaseTraits() != null && !studyDto.getDiseaseTraits().isEmpty()) {
                traitIds = studiesService.getTraitsIDsFromDB(studyDto.getDiseaseTraits(), studyId);
            }*/
            Study study = studyDtoAssembler.disassembleForExsitingStudy(studyDto, studyId);
            if (studyDto.getDiseaseTrait() != null ) {

                study.setDiseaseTrait(studyDto.getDiseaseTrait().getDiseaseTraitId());
            }
            if (studyDto.getEfoTraits() != null && !studyDto.getEfoTraits().isEmpty()) {
                efoTraitIds = studyDto.getEfoTraits().stream().map(EfoTraitDto::getEfoTraitId).collect(Collectors.toList());
                study.setEfoTraits(efoTraitIds);
            }
            if (studyDto.getBackgroundEfoTraits() != null) {
                efoTraitIds = studyDto.getBackgroundEfoTraits().stream().map(EfoTraitDto::getEfoTraitId).collect(Collectors.toList());
                study.setBackgroundEfoTraits(efoTraitIds);
            }

            Study studyUpdated = studiesService.updateStudies(study);
            return studyDtoAssembler.toResource(studyUpdated);
        } else {
            throw new EntityNotFoundException("Study not found "+ studyId);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}"+DepositionCurationConstants.API_STUDIES,produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public PagedResources<StudyDto> getStudies(PagedResourcesAssembler assembler,
                                               @PathVariable(value = DepositionCurationConstants.PARAM_SUBMISSION_ID)  String submissionId,
                                               @SortDefault(sort = "accession", direction = Sort.Direction.DESC)
                                                   @PageableDefault(size = 10, page = 0) Pageable pageable) {
        /*if(searchStudyDTO != null ) {
            log.info("searchStudyDTO Params are ->"+ searchStudyDTO.getReportedTrait());
        }*/
        Page<Study> studies =  studiesService.getStudies(submissionId, pageable);

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(StudiesController.class).getStudies(assembler, submissionId, pageable));

        return assembler.toResource(studies, studyDtoAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));


    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}"+DepositionCurationConstants.API_STUDIES+DepositionCurationConstants.API_SAMPLEDESCRIPTION,produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public PagedResources<StudySampleDescPatchRequest> getSampleDescription(PagedResourcesAssembler assembler,
                                                                            @PathVariable(value = DepositionCurationConstants.PARAM_SUBMISSION_ID)  String submissionId,
                                                                            @SortDefault(sort = "accession", direction = Sort.Direction.DESC)
                                                         @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<Study> studies =  studiesService.getStudies(submissionId, pageable);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(StudiesController.class).getSampleDescription(assembler, submissionId, pageable));

        return assembler.toResource(studies, studySampleDescPatchRequestAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));

    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{submissionId}"+DepositionCurationConstants.API_STUDIES+DepositionCurationConstants.API_SAMPLEDESCRIPTION,produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public List<StudySampleDescPatchRequest> patchSampleDescription(@PathVariable(value = DepositionCurationConstants.PARAM_SUBMISSION_ID) String submissionId,
                                                                    @Valid @RequestBody List<StudySampleDescPatchRequest> studySampleDescPatchRequests){

        List<StudySampleDescPatchRequest> sampleDescPatchRequests = studiesService.updateSampleDescription(studySampleDescPatchRequests, submissionId);
        studiesService.sendMetaDataMessageToQueue(submissionId);
        return sampleDescPatchRequests;

    }


}
