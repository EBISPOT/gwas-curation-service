
package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyPatchRequestAssembler;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitStudyMappingDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class StudyTraitsUploadFileController {

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    StudyPatchRequestAssembler studyPatchRequestAssembler;

    @Autowired
    StudiesService studiesService;

    @Autowired
    FileHandler fileHandler;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{submissionId}" + DepositionCurationConstants.API_STUDIES + DepositionCurationConstants.API_DISEASE_TRAITS + "/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HttpEntity<byte[]> uploadDiseaseTraitsStudyMappings(@RequestParam MultipartFile multipartFile,@PathVariable String submissionId,
                                                                       HttpServletRequest request) {
       /* if (result.hasErrors()) {
            throw   new FileValidationException(result);
        }*/
        if(multipartFile.isEmpty()){
            throw new FileProcessingException("File not found");
        }
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        List<StudyPatchRequest> studyPatchRequests = studyPatchRequestAssembler.disassemble(multipartFile);
        List<TraitUploadReport> traitUploadReport = studiesService.updateTraitsForStudies(studyPatchRequests, submissionId );
        byte[] result = fileHandler.serializePojoToTsv(traitUploadReport);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=studyTraitUploadReports.tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{submissionId}" + DepositionCurationConstants.API_STUDIES + DepositionCurationConstants.API_EFO_TRAITS + "/files")
    public HttpEntity<byte[]> uploadEfoStudyMapping(@PathVariable String submissionId, @RequestParam MultipartFile multipartFile, HttpServletRequest request) {

        if(multipartFile.isEmpty()){
            throw new FileProcessingException("File not found");
        }
        userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        List<EfoTraitStudyMappingDto> efoTraitStudyMappingDtos = studyPatchRequestAssembler.disassembleForEfoMapping(multipartFile);
        List<TraitUploadReport> traitUploadReport = studiesService.updateEfoTraitsForStudies(efoTraitStudyMappingDtos, submissionId);
        byte[] result = fileHandler.serializePojoToTsv(traitUploadReport);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=studyTraitUploadReports.tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }
}
