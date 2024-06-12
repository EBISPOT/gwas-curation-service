package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.audit.PublicationAuditEntryDto;
import uk.ac.ebi.spot.gwas.deposition.domain.User;

public interface PublicationAuditService {

   void createAuditEvent(String eventType, String subOrPubId, String event,
                         Boolean isPublication, User user);



}
