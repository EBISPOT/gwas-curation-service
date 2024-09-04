package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatusSnapshotStatsEntry;

public interface CurationStatusSnapshotStatsEntryRepository extends MongoRepository<CurationStatusSnapshotStatsEntry, String> {


}
