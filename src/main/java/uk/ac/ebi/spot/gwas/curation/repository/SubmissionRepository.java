package uk.ac.ebi.spot.gwas.curation.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public interface SubmissionRepository extends MongoRepository<Submission, String> {

    Optional<Submission> findByIdAndArchived(String id, boolean archived);

    Optional<Submission> findByIdAndArchivedAndCreated_UserId(String id, boolean archived, String userId);
}
