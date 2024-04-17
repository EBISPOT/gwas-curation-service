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
import uk.ac.ebi.spot.gwas.curation.rest.dto.MatchPublicationReportDTOAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MatchPublicationReport;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MatchPublicationReportDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationStatusReport;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_PUBLICATIONS)
public class PublicationsController {

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    PublicationService publicationService;

    @Autowired
    MatchPublicationReportDTOAssembler publicationReportDTOAssembler;
    @Autowired
    PublicationDtoAssembler publicationDtoAssembler;


    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{pmids}",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public List<PublicationStatusReport> createPublication(@PathVariable List<String> pmids, HttpServletRequest request ) {
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        return publicationService.createPublication(pmids, user);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{pmid}/linked-submissions",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public PagedResources<MatchPublicationReportDTO> matchPublication(PagedResourcesAssembler assembler, @PathVariable String pmid,
                                                                      @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<MatchPublicationReport> matchPublicationReports = publicationService.matchPublication(pmid, pageable);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PublicationsController.class).matchPublication(assembler, pmid, pageable));
        return assembler.toResource(matchPublicationReports, publicationReportDTOAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));
    }

    @PatchMapping(value = "/{pmid}")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public PublicationDto patchPublication(@PathVariable String pmid, @RequestBody PublicationDto publicationDto, HttpServletRequest request) {
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        return publicationService.patchPublication(pmid, publicationDto, user);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @GetMapping
    public PagedResources<PublicationDto> search(SearchPublicationDTO searchPublicationDTO,
                                                 PagedResourcesAssembler assembler,
                                                 @PageableDefault(size = 10, page = 0) Pageable pageable) throws IOException {
        Page<Publication> publications = publicationService.search(searchPublicationDTO, pageable);
        final ControllerLinkBuilder controllerLinkBuilder = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PublicationsController.class)
                .search(searchPublicationDTO, assembler, pageable)
        );
        return assembler.toResource(publications, publicationDtoAssembler,
                new Link(BackendUtil.underBasePath(controllerLinkBuilder, depositionCurationConfig.getProxy_prefix()).toUri().toString()));
    }

    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/utils/fill-submitter-for-old-publications")
    public void fillSubmitterForOldPublications() {
        publicationService.fillSubmitterForOldPublications();
    }


    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}")
    public Resource<PublicationDto> getPublication(@PathVariable String id) {
        Publication publication = publicationService.getPublicationDetailsByPmidOrPubId(id, false);
        if (publication != null) {
            return publicationDtoAssembler.toResource(publication);
        }
        else {
            throw new EntityNotFoundException("publication id not found, " + id);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{pmid}/link-submission")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public void linkSubmission(@PathVariable String pmid, @RequestParam String submissionId) {
        publicationService.linkSubmission(pmid, submissionId);
    }

}
