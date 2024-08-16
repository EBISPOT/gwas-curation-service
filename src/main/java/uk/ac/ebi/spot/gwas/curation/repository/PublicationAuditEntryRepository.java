package uk.ac.ebi.spot.gwas.curation.repository;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuditEntry;

import java.util.List;

public interface PublicationAuditEntryRepository extends MongoRepository<PublicationAuditEntry, String> {

      Page<PublicationAuditEntry> findByPublicationId(String pubId, Pageable pageable);

      List<PublicationAuditEntry> findByTimestampAfter(DateTime dateTime);
}
