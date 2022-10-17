
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
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.solr.dto.SolrPublicationDTOAssembler;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SolrPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_PUBLICATIONS)
public class PublicationSolrController {

        @Autowired
        PublicationService publicationService;

        @Autowired
        SolrPublicationDTOAssembler solrPublicationDTOAssembler;

        @Autowired
        DepositionCurationConfig depositionCurationConfig;


        @ResponseStatus(HttpStatus.OK)
        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public PagedResources<SolrPublicationDTO> getPublications(SearchPublicationDTO searchPublicationDTO,
                                                                  PagedResourcesAssembler assembler,
                                                                  @PageableDefault(size = 10, page = 0) Pageable pageable) {

           Page<SOLRPublication>  solrPublications = publicationService.searchPublications(searchPublicationDTO, pageable);
                final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                        .methodOn(PublicationSolrController.class).getPublications(searchPublicationDTO,assembler, pageable));

                return assembler.toResource(solrPublications, solrPublicationDTOAssembler,
                        new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));

        }

        @ResponseStatus(HttpStatus.OK)
        @GetMapping("/{pubId}")
        public Resource<SolrPublicationDTO> getPublication(@PathVariable String pubId) {
                SOLRPublication solrPublication = publicationService.getPublicationFromSolr(pubId);
                if(solrPublication != null){
                        return solrPublicationDTOAssembler.toResource(solrPublication);
                }else {
                        throw new EntityNotFoundException("Solr Entity not found ->"+pubId);
                }
        }

}

