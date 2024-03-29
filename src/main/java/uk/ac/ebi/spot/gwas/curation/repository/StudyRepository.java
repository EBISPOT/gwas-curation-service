package uk.ac.ebi.spot.gwas.curation.repository;

//import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public interface StudyRepository extends MongoRepository<Study, String> {

    Stream<Study> readByIdIn(List<String> ids);

    Stream<Study> findBySubmissionId(String submissionId);

    Optional<Study> findByAccession(String accession);

    Optional<Study> findByAccessionAndSubmissionId(String accession, String submissionId);

    Page<Study> findBySubmissionId(String submissionId, Pageable page);


    //Page<Study> findBySubmissionId(String submissionId, Pageable page, Predicate predicate);

    List<Study> findByDiseaseTrait(String traitId);

    Stream<Study> findByEfoTraitsContains(String traitId);
}
