package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatus;

public interface CurationStatusRepository extends MongoRepository<CurationStatus, String> {
}
