package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
    public void ingestStudies() {
        studySolrIndexerService.populateStudyIngestEntries();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping(value = DepositionCurationConstants.API_POPULATE_SOLR_STUDIES)
    public void reindexSolrStudies() {
        studySolrIndexerService.reindexSolrStudies();
    }
}
