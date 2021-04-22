package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.SubmissionRepository;
import uk.ac.ebi.spot.gwas.curation.service.CuratorAuthService;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionService;

import uk.ac.ebi.spot.gwas.deposition.constants.Status;
import uk.ac.ebi.spot.gwas.deposition.constants.SubmissionProvenanceType;
import uk.ac.ebi.spot.gwas.deposition.domain.*;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;


import java.util.ArrayList;
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
