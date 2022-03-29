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

import java.util.Arrays;
import java.util.List;
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
                 List<String> metaStatus = null;
                 List<String> ssStatus = null;
                 List<String> overAllStatus = null;
                 List<String> lockStatus = null;
                 if(searchSubmissionDTO.getMetaStatus() != null){
                    metaStatus = Arrays.asList(searchSubmissionDTO.getMetaStatus().split("\\|"));
                    }

                 if(searchSubmissionDTO.getSsStatus() != null){
                     ssStatus = Arrays.asList(searchSubmissionDTO.getSsStatus().split("\\|"));
                 }

                 if(searchSubmissionDTO.getSubmissionStatus() != null){
                     overAllStatus = Arrays.asList(searchSubmissionDTO.getSubmissionStatus().split("\\|"));
                 }

                 if(searchSubmissionDTO.getLockStatus() != null){
                     lockStatus = Arrays.asList(searchSubmissionDTO.getLockStatus().split("\\|"));
                 }

                if(pubId != null)
                    return submissionRepository.findByPublicationIdAndArchived(pubId, false, page);
                else if(bowId != null)
                    return submissionRepository.findByBodyOfWorksContainsAndArchived(bowId, false, page);
                else if(metaStatus != null && ssStatus != null && overAllStatus != null && lockStatus != null )
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, ssStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && ssStatus != null && overAllStatus != null)
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndOverallStatusInAndArchived(
                            metaStatus, ssStatus, overAllStatus, false, page);
                else if(metaStatus != null && ssStatus != null && lockStatus != null)
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, ssStatus, lockStatus, false, page);
                else if(ssStatus != null && overAllStatus != null && lockStatus != null)
                    return  submissionRepository.findBySummaryStatsStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(
                            ssStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && overAllStatus != null && lockStatus != null)
                    return  submissionRepository.findByMetadataStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && ssStatus != null)
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndArchived(
                            metaStatus, ssStatus, false, page);
                else if(metaStatus != null && overAllStatus != null)
                    return submissionRepository.findByMetadataStatusInAndOverallStatusInAndArchived(
                            metaStatus, overAllStatus, false, page);
                else if(ssStatus != null && overAllStatus != null)
                    return submissionRepository.findBySummaryStatsStatusInAndOverallStatusInAndArchived(
                            ssStatus, overAllStatus, false, page);
                else if(metaStatus != null && lockStatus != null)
                    return submissionRepository.findByMetadataStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, lockStatus, false, page);
                else if(ssStatus != null && lockStatus != null)
                    return submissionRepository.findBySummaryStatsStatusInAndLockDetails_StatusInAndArchived(
                            ssStatus, lockStatus, false, page);
                else if(metaStatus != null)
                    return submissionRepository.findByMetadataStatusInAndArchived(metaStatus , false, page);
                else if(ssStatus != null)
                    return submissionRepository.findBySummaryStatsStatusInAndArchived(ssStatus , false, page);
                else if(overAllStatus != null)
                    return submissionRepository.findByOverallStatusInAndArchived(overAllStatus , false, page);
                else if(lockStatus != null)
                    return submissionRepository.findByLockDetails_StatusInAndArchived(lockStatus , false, page);
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
