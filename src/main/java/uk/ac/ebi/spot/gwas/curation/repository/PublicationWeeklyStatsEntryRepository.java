package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuditEntry;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationWeeklyStatsEntry;

public interface PublicationWeeklyStatsEntryRepository extends MongoRepository<PublicationWeeklyStatsEntry, String> {


}
