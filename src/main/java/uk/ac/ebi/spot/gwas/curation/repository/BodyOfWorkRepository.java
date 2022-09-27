package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.BodyOfWork;

import java.util.Optional;

public interface BodyOfWorkRepository extends MongoRepository<BodyOfWork, String> {


    Optional<BodyOfWork> findByBowIdAndArchived(String bodyOfWorkId, boolean archived);
    Optional<BodyOfWork> findByBowId(String bodyOfWorkId);
}
