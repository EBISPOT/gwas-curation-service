package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.deposition.domain.StudyIngestEntry;

@Component
public class StudySolrAssembler {

    public static StudySolr assemble(StudyIngestEntry studyIngestEntry) {
        return new StudySolr(studyIngestEntry.getReportedTrait(),
                studyIngestEntry.getEfoTraits(),
                studyIngestEntry.getNotes(),
                studyIngestEntry.getPmid(),
                studyIngestEntry.getSubmissionId(),
                studyIngestEntry.getPublicationDate(),
                studyIngestEntry.getFirstAuthor(),
                studyIngestEntry.getTitle(),
                studyIngestEntry.getSumstatsFlag(),
                studyIngestEntry.getPooledFlag(),
                studyIngestEntry.getGxeFlag(),
                studyIngestEntry.getAccessionId(),
                studyIngestEntry.getBowId());
    }

}
