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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.CurationStatusDTOAssembler;
import uk.ac.ebi.spot.gwas.curation.service.CurationStatusService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatus;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.CurationStatusDTO;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_PUBLICATION_STATUS)
public class CurationStatusController {

    @Autowired
    CurationStatusDTOAssembler curationStatusDTOAssembler;

    @Autowired
    CurationStatusService curationStatusService;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public PagedResources<CurationStatusDTO> getAllCurationStatus(PagedResourcesAssembler assembler,
                                                               @PageableDefault(size = 10, page = 0) Pageable pageable) {
       Page<CurationStatus> curationStatuses = curationStatusService.findAllCurationStatus(pageable);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(CurationStatusController.class).getAllCurationStatus(assembler, pageable));

        return assembler.toResource(curationStatuses, curationStatusDTOAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{curatorStatusId}")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<CurationStatusDTO> getCurationStatus(@PathVariable String curatorStatusId) {
        CurationStatus curationStatus = curationStatusService.findCurationStatus(curatorStatusId);
        if(curationStatus != null){
            return curationStatusDTOAssembler.toResource(curationStatus);
        } else{
            throw new EntityNotFoundException("Entity not found ->"+curatorStatusId);
        }
    }

}
