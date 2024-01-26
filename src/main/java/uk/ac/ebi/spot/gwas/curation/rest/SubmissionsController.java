package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionService;
import uk.ac.ebi.spot.gwas.curation.service.impl.SubmissionAssemblyService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.dto.SubmissionDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchSubmissionDTO;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class SubmissionsController {

    @Autowired
    PublicationService publicationService;

    @Autowired
    SubmissionService submissionService;

    @Autowired
    SubmissionAssemblyService submissionAssemblyService;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource<SubmissionDto> getSubmission(@PathVariable("submissionId") String submissionId) {
         Submission submission =   submissionService.getSubmission(submissionId);
         return submissionAssemblyService.toResource(submission);
    }

    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PagedResources<SubmissionDto> getSubmissions(HttpServletRequest request,
                                                        SearchSubmissionDTO searchSubmissionDTO,
                                                        @PageableDefault(size = 20, page = 0) Pageable pageable,
                                                        PagedResourcesAssembler assembler) {
        String pmid = null;

        if(searchSubmissionDTO != null)
            pmid = searchSubmissionDTO.getPmid() != null ? searchSubmissionDTO.getPmid() : null;

        Publication publication = null;

        if(pmid != null)
            publication  = publicationService.getPublicationDetailsByPmidOrPubId(pmid, true);

        Page<Submission> submissions = submissionService.getSubmissions( publication != null ? publication.getId() : null , searchSubmissionDTO, pageable);

        final ControllerLinkBuilder  lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SubmissionsController.class)
                                            .getSubmissions(request, searchSubmissionDTO, pageable,  assembler));

        return assembler.toResource(submissions , submissionAssemblyService, new Link(
                BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));

    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{submissionId}")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<SubmissionDto> patchSubmission(@PathVariable String submissionId, @RequestBody SubmissionDto submissionDto) {

        return submissionAssemblyService.toResource(submissionService.patchSubmission(submissionDto, submissionId));
    }

}
