package uk.ac.ebi.spot.gwas.curation.service;

public interface StudySolrIndexerService {

    public void populateStudyIngestEntries();

    public void reindexSolrStudies();
}
