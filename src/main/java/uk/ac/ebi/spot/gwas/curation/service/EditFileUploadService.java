package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;

import java.io.IOException;

public interface EditFileUploadService {

    public ResponseEntity <Resource<FileUploadDto>> uploadEditFIle(String jwtToken, String submissionId, MultipartFile file) ;

}
