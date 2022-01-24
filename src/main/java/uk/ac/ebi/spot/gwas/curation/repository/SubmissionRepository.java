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

    Page<Submission> findByMetadataStatusAndArchived(String status, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusAndArchived(String status, Boolean archived, Pageable page );

    Page<Submission> findByOverallStatusAndArchived(String status, Boolean archived, Pageable page );

    Page<Submission> findByLockDetails_StatusAndArchived(String status, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusAndSummaryStatsStatusAndArchived(String metaStatus,String ssStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusAndOverallStatusAndArchived(String metaStatus,String overallStatus, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusAndOverallStatusAndArchived(String ssStatus,String overallStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusAndLockDetails_StatusAndArchived(String metaStatus,String lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByOverallStatusAndLockDetails_StatusAndArchived(String overallStatus,String lockStatus, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusAndLockDetails_StatusAndArchived(String ssStatus, String lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusAndSummaryStatsStatusAndLockDetails_StatusAndArchived(String metaStatus, String ssStatus, String lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusAndSummaryStatsStatusAndOverallStatusAndArchived(String metaStatus,String ssStatus, String overallStatus, Boolean archived, Pageable page );

    Page<Submission> findBySummaryStatsStatusAndOverallStatusAndLockDetails_StatusAndArchived(String ssStatus,String overallStatus, String lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusAndOverallStatusAndLockDetails_StatusAndArchived(String metaStatus,String overallStatus, String lockStatus, Boolean archived, Pageable page );

    Page<Submission> findByMetadataStatusAndSummaryStatsStatusAndOverallStatusAndLockDetails_StatusAndArchived(String metaStatus,String ssStatus,String overallStatus, String lockStatus, Boolean archived, Pageable page );


}
