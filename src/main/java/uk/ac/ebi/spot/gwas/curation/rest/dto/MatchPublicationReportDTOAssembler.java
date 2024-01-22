package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.PublicationsController;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MatchPublicationReport;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MatchPublicationReportDTO;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;

@Component
public class MatchPublicationReportDTOAssembler implements ResourceAssembler<MatchPublicationReport, Resource<MatchPublicationReportDTO>> {

        @Autowired
        DepositionCurationConfig depositionCurationConfig;

        @Override
        public Resource<MatchPublicationReportDTO> toResource(MatchPublicationReport matchPublicationReport) {

                MatchPublicationReportDTO matchPublicationReportDTO = MatchPublicationReportDTO
                        .builder()
                        .pubMedID(matchPublicationReport.getPubMedID())
                        .submissionID(matchPublicationReport.getSubmissionID())
                        .author(matchPublicationReport.getAuthor())
                        .title(matchPublicationReport.getTitle())
                        .doi(matchPublicationReport.getDoi())
                        .cosScore(matchPublicationReport.getCosScore())
                        .levDistance(matchPublicationReport.getLevDistance())
                        .build();

                final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                        ControllerLinkBuilder.methodOn(PublicationsController.class).matchPublication(null, matchPublicationReport.getPubMedID(), null));

                Resource<MatchPublicationReportDTO> resource = new Resource<>(matchPublicationReportDTO);
                resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
                return resource;
        }
}
