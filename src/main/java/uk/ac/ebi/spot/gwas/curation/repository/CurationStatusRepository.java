package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatus;

import java.util.Optional;

public interface CurationStatusRepository extends MongoRepository<CurationStatus, String> {

    Optional<CurationStatus> findCurationStatusByStatus(String status);

}
