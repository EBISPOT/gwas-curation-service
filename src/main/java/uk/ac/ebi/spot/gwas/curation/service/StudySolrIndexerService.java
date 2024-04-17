package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.StudyIngestEntry;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyRabbitMessage;

public interface StudySolrIndexerService {

    public void populateStudyIngestEntries();

    public void reindexSolrStudies();

    public void removeSolrStudies();

    public StudySolr getDetailsFromSolr(String seqId);

    void syncSolrWithStudies(StudyRabbitMessage studyRabbitMessage);
}
