package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationQueueStatsEntry;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatusSnapshotStatsEntry;

public interface CurationQueueStatsEntryRepository  extends MongoRepository<CurationQueueStatsEntry, String> {
}
