
package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyPatchRequestAssembler;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.FileUploadRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;
import uk.ac.ebi.spot.gwas.deposition.exception.FileValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_STUDIES_TRAITS_UPLOAD)
public class StudyTraitsUploadFileController {

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    StudyPatchRequestAssembler studyPatchRequestAssembler;

    @Autowired
    StudiesService studiesService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TraitUploadReport>> uploadDiseaseTraitsStudyMappings(@RequestParam MultipartFile multipartFile,
                                                                       HttpServletRequest request) {
       /* if (result.hasErrors()) {
            throw   new FileValidationException(result);
        }*/
        if(multipartFile.isEmpty()){
            throw new FileProcessingException("File not found");
        }
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        List<StudyPatchRequest> studyPatchRequests = studyPatchRequestAssembler.disassemble(multipartFile);
        List<TraitUploadReport> traitUploadReport = studiesService.updateTraitsForStudies(studyPatchRequests);
        return new ResponseEntity<>(traitUploadReport, HttpStatus.CREATED);
    }

}
