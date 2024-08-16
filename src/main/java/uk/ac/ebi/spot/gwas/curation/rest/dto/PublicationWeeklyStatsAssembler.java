package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.service.impl.PublicationAuditEntryServiceImpl;
import uk.ac.ebi.spot.gwas.deposition.audit.PublicationWeeklyStats;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationWeeklyStatsEntry;

@Component
public class PublicationWeeklyStatsAssembler {

    private static final Logger log = LoggerFactory.getLogger(PublicationAuditEntryServiceImpl.class);

    public PublicationWeeklyStatsEntry assemble(PublicationWeeklyStats publicationWeeklyStats) {
        PublicationWeeklyStatsEntry publicationWeeklyStatsEntry = new PublicationWeeklyStatsEntry();
        publicationWeeklyStatsEntry.setPmidsAdded(publicationWeeklyStats.getPmidsAdded());
        publicationWeeklyStatsEntry.setPmidsPublished(publicationWeeklyStats.getPmidsPublished());
        publicationWeeklyStatsEntry.setPmidsLevel2Done(publicationWeeklyStats.getPmidsLevel2Done());
        publicationWeeklyStatsEntry.setPmidsSubComplete(publicationWeeklyStats.getPmidsSubComplete());
        publicationWeeklyStatsEntry.setPmidsSingleLevelComplete(publicationWeeklyStats.getPmidsSingleLevelComplete());
        publicationWeeklyStatsEntry.setTimestamp(DateTime.now());
        return publicationWeeklyStatsEntry;

    }
}
