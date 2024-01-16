package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.CurationStatusController;
import uk.ac.ebi.spot.gwas.curation.rest.CuratorController;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatus;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.CurationStatusDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.CuratorDTO;

@Component
public class CurationStatusDTOAssembler implements ResourceAssembler<CurationStatus, Resource<CurationStatusDTO>> {

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Override
    public Resource<CurationStatusDTO> toResource(CurationStatus curationStatus) {
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(CurationStatusController.class).getCurationStatus(curationStatus.getId()));

        Resource<CurationStatusDTO> resource = new Resource<>(assemble(curationStatus));
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;
    }

    public CurationStatusDTO assemble(CurationStatus curationStatus) {
        return CurationStatusDTO
                .builder()
                .id(curationStatus.getId())
                .status(curationStatus.getStatus())
                .build();
    }
}
