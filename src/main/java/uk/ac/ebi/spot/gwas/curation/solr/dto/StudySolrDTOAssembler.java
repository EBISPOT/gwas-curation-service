package uk.ac.ebi.spot.gwas.curation.solr.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.FilterStudiesController;
import uk.ac.ebi.spot.gwas.curation.rest.SolrStudyController;
import uk.ac.ebi.spot.gwas.curation.rest.StudiesController;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;

@Component
public class StudySolrDTOAssembler implements ResourceAssembler<StudySolr, Resource<StudySolrDTO>> {

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Override
    public Resource<StudySolrDTO> toResource(StudySolr studySolr) {
        StudySolrDTO studySolrDTO = StudySolrDTO.builder()
                                    .accessionId(studySolr.getAccessionId())
                                    .efoTraits(studySolr.getEfoTraits())
                                    .reportedTrait(studySolr.getReportedTrait())
                                    .pmid(studySolr.getPmid())
                                    .submissionId(studySolr.getSubmissionId())
                                    .bowId(studySolr.getBowId())
                                    .publicationDate(studySolr.getPublicationDate())
                                    .firstAuthor(studySolr.getFirstAuthor())
                                    .title(studySolr.getTitle())
                                    .gxeFlag(studySolr.getGxeFlag())
                                    .sumstatsFlag(studySolr.getSumstatsFlag())
                                    .pooledFlag(studySolr.getPooledFlag())
                                    .notes(studySolr.getNotes())
                                    .build();

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(SolrStudyController.class).getStudyFromSolr(studySolr.getId()));

        Resource<StudySolrDTO> resource = new Resource<>(studySolrDTO);
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;

    }
}
