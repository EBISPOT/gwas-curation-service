package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;
import uk.ac.ebi.spot.gwas.deposition.dto.SubmissionDto;

import java.io.IOException;

public interface EditFileUploadService {

    public ResponseEntity<Resource<FileUploadDto>> uploadEditFIle(String jwtToken, String submissionId, MultipartFile file) ;

    public ResponseEntity<Resource<SubmissionDto>> lockSubmission(String jwtToken, String lockStatus, String submissionId);




}
