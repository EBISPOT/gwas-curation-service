package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.SubmissionDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchSubmissionDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SubmissionEnvelope;


import java.util.Map;

public interface SubmissionService {


    Page<Submission> getSubmissions(String pubId, SearchSubmissionDTO searchSubmissionDTO, Pageable page);

    Submission getSubmission(String submissionId, User user);

    Submission getSubmission(String submissionId);

    Map<String, SubmissionEnvelope> getSubmissions();

    Submission patchSubmission(SubmissionDto submissionDto, String submissionId);


}
