package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.audit.PublicationAuditEntryDto;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuditEntry;

public interface PublicationAuditEntryService {


    PublicationAuditEntry getAuditEntry(String auditEntryId);


    Page<PublicationAuditEntry> getPublicationAuditEntries(String pubId, Pageable pageable);



}
