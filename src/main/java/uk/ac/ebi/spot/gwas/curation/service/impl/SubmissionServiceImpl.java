package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.SubmissionRepository;
import uk.ac.ebi.spot.gwas.curation.service.CuratorAuthService;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionService;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchSubmissionDTO;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.Optional;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private CuratorAuthService curatorAuthService;

    @Override
    public Submission getSubmission(String submissionId) {
        Optional<Submission> optionalSubmission = submissionRepository.findByIdAndArchived(submissionId, false);

        if (!optionalSubmission.isPresent()) {
            log.error("Unable to find submission: {}", submissionId);
            throw new EntityNotFoundException("Unable to find submission: " + submissionId);
        }
        log.info("Submission successfully retrieved: {}", optionalSubmission.get().getId());
        return optionalSubmission.get();
    }

    @Override
    public Page<Submission> getSubmissions(String pubId, SearchSubmissionDTO searchSubmissionDTO, Pageable page) {


             if(searchSubmissionDTO != null){
                String bowId = searchSubmissionDTO.getBowId();
                String metaStatus = searchSubmissionDTO.getMetaStatus();
                String ssStatus = searchSubmissionDTO.getSsStatus();
                String overAllStatus = searchSubmissionDTO.getSubmissionStatus();
                String lockStatus = searchSubmissionDTO.getLockStatus();
                if(pubId != null)
                    return submissionRepository.findByPublicationIdAndArchived(pubId, false, page);
                else if(bowId != null)
                    return submissionRepository.findByBodyOfWorksContainsAndArchived(bowId, false, page);
                if(metaStatus != null && ssStatus != null && overAllStatus != null && lockStatus != null )
                    return submissionRepository.findByMetadataStatusAndSummaryStatsStatusAndOverallStatusAndLockDetails_StatusAndArchived(
                            metaStatus, ssStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && ssStatus != null && overAllStatus != null)
                    return submissionRepository.findByMetadataStatusAndSummaryStatsStatusAndOverallStatusAndArchived(
                            metaStatus, ssStatus, overAllStatus, false, page);
                else if(metaStatus != null && ssStatus != null && lockStatus != null)
                    return submissionRepository.findByMetadataStatusAndSummaryStatsStatusAndLockDetails_StatusAndArchived(
                            metaStatus, ssStatus, lockStatus, false, page);
                else if(ssStatus != null && overAllStatus != null && lockStatus != null)
                    return  submissionRepository.findBySummaryStatsStatusAndOverallStatusAndLockDetails_StatusAndArchived(
                            ssStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && overAllStatus != null && lockStatus != null)
                    return  submissionRepository.findByMetadataStatusAndOverallStatusAndLockDetails_StatusAndArchived(
                            metaStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && ssStatus != null)
                    return submissionRepository.findByMetadataStatusAndSummaryStatsStatusAndArchived(
                            metaStatus, ssStatus, false, page);
                else if(metaStatus != null && overAllStatus != null)
                    return submissionRepository.findByMetadataStatusAndOverallStatusAndArchived(
                            metaStatus, overAllStatus, false, page);
                else if(ssStatus != null && overAllStatus != null)
                    return submissionRepository.findBySummaryStatsStatusAndOverallStatusAndArchived(
                            ssStatus, overAllStatus, false, page);
                else if(metaStatus != null && lockStatus != null)
                    return submissionRepository.findByMetadataStatusAndLockDetails_StatusAndArchived(
                            metaStatus, lockStatus, false, page);
                else if(ssStatus != null && lockStatus != null)
                    return submissionRepository.findBySummaryStatsStatusAndLockDetails_StatusAndArchived(
                            ssStatus, lockStatus, false, page);
                else if(metaStatus != null)
                    return submissionRepository.findByMetadataStatusAndArchived(metaStatus , false, page);
                else if(ssStatus != null)
                    return submissionRepository.findBySummaryStatsStatusAndArchived(ssStatus , false, page);
                else if(overAllStatus != null)
                    return submissionRepository.findByOverallStatusAndArchived(overAllStatus , false, page);
                else if(lockStatus != null)
                    return submissionRepository.findByLockDetails_StatusAndArchived(lockStatus , false, page);
            }

             return submissionRepository.findByArchived(false, page);
    }


    @Override
    public Submission getSubmission(String submissionId, User user) {
        log.info("Retrieving submission: {}", submissionId);
        Optional<Submission> optionalSubmission = curatorAuthService.isCurator(user) ?
                submissionRepository.findByIdAndArchived(submissionId, false) :
                submissionRepository.findByIdAndArchivedAndCreated_UserId(submissionId, false, user.getId());
        if (!optionalSubmission.isPresent()) {
            log.error("Unable to find submission: {}", submissionId);
            throw new EntityNotFoundException("Unable to find submission: " + submissionId);
        }
        log.info("Submission successfully retrieved: {}", optionalSubmission.get().getId());
        return optionalSubmission.get();
    }






}
