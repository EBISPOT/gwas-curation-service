package uk.ac.ebi.spot.gwas.curation.service;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationQueueStats;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationStatusSnapshotStats;
import uk.ac.ebi.spot.gwas.deposition.audit.PublicationAuditEntryDto;
import uk.ac.ebi.spot.gwas.deposition.audit.PublicationWeeklyStats;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuditEntry;

import java.util.List;

public interface PublicationAuditEntryService {


    PublicationAuditEntry getAuditEntry(String auditEntryId);


    Page<PublicationAuditEntry> getPublicationAuditEntries(String pubId, Pageable pageable);


     PublicationWeeklyStats getPublicationStats(DateTime dateTime) ;


    CurationStatusSnapshotStats getCurationStatusSnapshotStats(DateTime dateTime);


    List<CurationQueueStats> getCurationQueueStats();



}
