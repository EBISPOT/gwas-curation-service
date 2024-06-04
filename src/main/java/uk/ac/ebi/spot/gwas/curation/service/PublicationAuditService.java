package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.audit.PublicationAuditEntryDto;

public interface PublicationAuditService {

   void createAuditEvent(PublicationAuditEntryDto publicationAuditEntryDto);



}
