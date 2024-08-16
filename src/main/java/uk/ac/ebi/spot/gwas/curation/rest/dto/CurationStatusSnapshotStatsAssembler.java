package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationStatusSnapshotStats;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatusSnapshotStatsEntry;

@Component
public class CurationStatusSnapshotStatsAssembler {

    public CurationStatusSnapshotStatsEntry assemble(CurationStatusSnapshotStats curationStatusSnapshotStats) {
        CurationStatusSnapshotStatsEntry curationStatusSnapshotStatsEntry = new CurationStatusSnapshotStatsEntry();
        curationStatusSnapshotStatsEntry.setAwaitSubmissionCount(curationStatusSnapshotStats.getAwaitSubmissionCount());
        curationStatusSnapshotStatsEntry.setAwaitingEFOMapping(curationStatusSnapshotStats.getAwaitingEFOMapping());
        curationStatusSnapshotStatsEntry.setAwaitingLevel2Completion(curationStatusSnapshotStats.getAwaitingLevel2Completion());
        curationStatusSnapshotStatsEntry.setAwaitingLevel2CompletionWithAccsns(curationStatusSnapshotStats.getAwaitingLevel2CompletionWithAccsns());
        curationStatusSnapshotStatsEntry.setAwaitLiteratureCount(curationStatusSnapshotStats.getAwaitLiteratureCount());
        curationStatusSnapshotStatsEntry.setOutstandingQuery(curationStatusSnapshotStats.getOutstandingQuery());
        curationStatusSnapshotStatsEntry.setTotalPublished(curationStatusSnapshotStats.getTotalPublished());
        curationStatusSnapshotStatsEntry.setTimestamp(DateTime.now());
        return curationStatusSnapshotStatsEntry;
    }
}
