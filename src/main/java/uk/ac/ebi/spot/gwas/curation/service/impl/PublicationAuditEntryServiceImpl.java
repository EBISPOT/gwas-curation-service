package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuditEntryRepository;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuditEntryService;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuditEntry;

@Service
public class PublicationAuditEntryServiceImpl implements PublicationAuditEntryService {

    private static final Logger log = LoggerFactory.getLogger(PublicationAuditEntryServiceImpl.class);
    @Autowired
    PublicationAuditEntryRepository publicationAuditEntryRepository;


    public PublicationAuditEntry getAuditEntry(String auditEntryId) {
      return   publicationAuditEntryRepository.findById(auditEntryId)
                .orElse(null);

    }


   public  Page<PublicationAuditEntry> getPublicationAuditEntries(String pubId, Pageable pageable){
      return publicationAuditEntryRepository.findByPublicationId(pubId, pageable);
   }


}


