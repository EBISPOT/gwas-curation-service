package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.StudySolrIndexerService;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;

@RestController
@RequestMapping(value = GeneralCommon.API_V1)
public class StudyIngestServiceController {

    @Autowired
    StudySolrIndexerService  studySolrIndexerService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping(value = DepositionCurationConstants.API_INGEST_STUDIES)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public void ingestStudies() {
        studySolrIndexerService.populateStudyIngestEntries();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping(value = DepositionCurationConstants.API_POPULATE_SOLR_STUDIES)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public void reindexSolrStudies() {
        studySolrIndexerService.reindexSolrStudies();
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = DepositionCurationConstants.API_SOLR_REMOVE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public void removeStudies() {
        studySolrIndexerService.removeSolrStudies();
    }

}
