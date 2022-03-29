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

    Page<Submission> findByArchived(Boolean archived, Pageable page);

    Page<Submission> findByPublicationIdAndArchived(String pubId,Boolean archived, Pageable page);

    Page<Submission> findByBodyOfWorksContainsAndArchived(String pubId, Boolean archived, Pageable page);

    Page<Submission> findByMetadataStatusInAndArchived(List<String> status, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusInAndArchived(List<String> status, Boolean archived, Pageable page );

    Page<Submission> findByOverallStatusInAndArchived(List<String> status, Boolean archived, Pageable page );

    Page<Submission> findByLockDetails_StatusInAndArchived(List<String> status, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusInAndSummaryStatsStatusInAndArchived(List<String> metaStatus,List<String> ssStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusInAndOverallStatusInAndArchived(List<String> metaStatus,List<String> overallStatus, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusInAndOverallStatusInAndArchived(List<String> ssStatus,List<String>  overallStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusInAndLockDetails_StatusInAndArchived(List<String> metaStatus,List<String> lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByOverallStatusInAndLockDetails_StatusInAndArchived(List<String> overallStatus,List<String> lockStatus, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusInAndLockDetails_StatusInAndArchived(List<String> ssStatus, List<String> lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusInAndSummaryStatsStatusInAndLockDetails_StatusInAndArchived(List<String> metaStatus, List<String> ssStatus, List<String> lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusInAndSummaryStatsStatusInAndOverallStatusInAndArchived(List<String> metaStatus,List<String> ssStatus, List<String> overallStatus, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(List<String> ssStatus,List<String> overallStatus, List<String> lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(List<String> metaStatus,List<String> overallStatus, List<String> lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusInAndSummaryStatsStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(List<String> metaStatus,List<String> ssStatus,List<String> overallStatus, List<String> lockStatus, Boolean archived, Pageable page );


}
