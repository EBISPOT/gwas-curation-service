package uk.ac.ebi.spot.gwas.curation.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_STUDIES)
public class StudiesController {

    private static final Logger log = LoggerFactory.getLogger(StudiesController.class);

    @Autowired
    StudiesService studiesService;

    @Autowired
    StudyDtoAssembler studyDtoAssembler;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{studyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource<StudyDto> getStudy(@PathVariable String studyId) {
        Study study = studiesService.getStudy(studyId);
        if( study != null ) {
            return studyDtoAssembler.toResource(study);
        } else {
            throw new EntityNotFoundException("Study not found "+ studyId);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{studyId}/diseasetraits", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResources<StudyDto>  getDiseaseTraits(PagedResourcesAssembler assembler,@PathVariable String studyId) {
        List<DiseaseTrait> diseaseTraits = studiesService.getDiseaseTraitsByStudyId(studyId);
        Pageable pageable = new PageRequest(0 , diseaseTraits.size());
        Page<DiseaseTrait> traitPage = new PageImpl<>(diseaseTraits, pageable,  diseaseTraits.size());

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(StudiesController.class).getDiseaseTraits(assembler, studyId));

        return assembler.toResource(traitPage, diseaseTraitDtoAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{studyId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public  Resource<StudyDto> updateStudies(@PathVariable String studyId, @Valid @RequestBody StudyDto studyDto, HttpServletRequest request) {
        List<String> traitIds = null;
        log.info("Disease Traits from request:"+studyDto.getDiseaseTraits());
        if(studyDto.getDiseaseTraits() !=null && !studyDto.getDiseaseTraits().isEmpty())  {
           traitIds = studiesService.getTraitsIDsFromDB(studyDto.getDiseaseTraits(), studyId);
       }
        Study study = studyDtoAssembler.disassembleForExsitingStudy(studyDto, studyId);
        study.setDiseaseTraits(traitIds);
        Study studyUpdated = studiesService.updateStudies(study);
        return studyDtoAssembler.toResource(studyUpdated);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResources<StudyDto> getStudies(PagedResourcesAssembler assembler,
                                               @RequestParam(value = DepositionCurationConstants.PARAM_SUBMISSION_ID, required = true )  String submissionId,
                                               @SortDefault(sort = "accession", direction = Sort.Direction.DESC)
                                                   @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<Study> studies =  studiesService.getStudies(submissionId, pageable);

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(StudiesController.class).getStudies(assembler, submissionId, pageable));

        return assembler.toResource(studies, studyDtoAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));


    }

}
