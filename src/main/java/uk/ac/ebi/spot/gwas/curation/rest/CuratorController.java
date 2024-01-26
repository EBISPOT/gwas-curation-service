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
import uk.ac.ebi.spot.gwas.curation.rest.dto.CuratorDTOAssembler;
import uk.ac.ebi.spot.gwas.curation.service.CuratorService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.Curator;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.CuratorDTO;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_CURATORS)
public class CuratorController {

    @Autowired
    CuratorService curatorService;

    @Autowired
    CuratorDTOAssembler curatorDTOAssembler;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public PagedResources<CuratorDTO> getCurators(PagedResourcesAssembler assembler,
                                                  @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<Curator> curators = curatorService.findAllCurators(pageable);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(CuratorController.class).getCurators(assembler, pageable));

        return assembler.toResource(curators, curatorDTOAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));

    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{curatorId}")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<CuratorDTO> getCuratorDetails(@PathVariable String curatorId) {
        Curator curator = curatorService.findCuratorDetails(curatorId);
        if(curator != null){
            return curatorDTOAssembler.toResource(curator);
        }else {
            throw new EntityNotFoundException("Solr Entity not found ->"+curatorId);
        }
    }


}
