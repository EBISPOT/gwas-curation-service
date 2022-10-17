package uk.ac.ebi.spot.gwas.curation.solr.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.PublicationSolrController;
import uk.ac.ebi.spot.gwas.curation.rest.SolrStudyController;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SolrPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

@Component
public class SolrPublicationDTOAssembler implements ResourceAssembler<SOLRPublication, Resource<SolrPublicationDTO>> {

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Override
    public Resource<SolrPublicationDTO> toResource(SOLRPublication solrPublication) {
        SolrPublicationDTO solrPublicationDTO = SolrPublicationDTO.builder()
                                                .pmid(solrPublication.getPmid())
                                                .publicationDate(solrPublication.getPublicationDate())
                                                .firstAuthor(solrPublication.getFirstAuthor())
                                                .title(solrPublication.getTitle())
                                                .correspondingAuthor(solrPublication.getCorrespondingAuthor())
                                                .journal(solrPublication.getJournal())
                                                .status(solrPublication.getStatus())
                                                .curationStatus(solrPublication.getCurationStatus())
                                                .curator(solrPublication.getCurator())
                                                .submitter(solrPublication.getSubmitter())
                                                .build();
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PublicationSolrController.class).getPublication(solrPublication.getId()));

        Resource<SolrPublicationDTO> resource = new Resource<>(solrPublicationDTO);
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;


    }
}
