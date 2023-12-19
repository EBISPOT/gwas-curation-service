package uk.ac.ebi.spot.gwas.curation.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudySolrAssembler;
import uk.ac.ebi.spot.gwas.curation.service.StudySolrIndexerService;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.curation.solr.dto.StudySolrDTO;
import uk.ac.ebi.spot.gwas.curation.solr.dto.StudySolrDTOAssembler;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SOLR_STUDIES)
public class SolrStudyController {

    @Autowired
    StudySolrIndexerService studySolrIndexerService;

    @Autowired
    StudySolrDTOAssembler studySolrAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{studyId}")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<StudySolrDTO> getStudyFromSolr(@PathVariable String studyId) {
        StudySolr studySolr = studySolrIndexerService.getDetailsFromSolr(studyId);
        if(studySolr != null) {
            return studySolrAssembler.toResource(studySolr);
        } else {
            throw new EntityNotFoundException("Solr Entity not found ->"+studyId);
        }

    }



}
