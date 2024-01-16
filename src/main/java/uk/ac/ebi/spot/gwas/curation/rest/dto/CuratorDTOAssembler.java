package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.CuratorController;
import uk.ac.ebi.spot.gwas.curation.rest.SolrStudyController;
import uk.ac.ebi.spot.gwas.curation.solr.dto.StudySolrDTO;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Curator;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.CuratorDTO;

@Component
public class CuratorDTOAssembler implements ResourceAssembler<Curator, Resource<CuratorDTO>> {

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Override
    public Resource<CuratorDTO> toResource(Curator curator) {
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(CuratorController.class).getCuratorDetails(curator.getId()));

        Resource<CuratorDTO> resource = new Resource<>(assemble(curator));
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;
    }

    public CuratorDTO assemble(Curator curator) {
        return CuratorDTO
                .builder()
                .id(curator.getId())
                .email(curator.getEmail())
                .firstName(curator.getFirstName())
                .lastName(curator.getLastName())
                .username(curator.getUsername())
                .build();
    }
}
