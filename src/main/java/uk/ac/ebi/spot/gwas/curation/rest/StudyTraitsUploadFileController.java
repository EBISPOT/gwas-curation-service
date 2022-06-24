
package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudySampleDescPatchRequestAssembler;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class StudyTraitsUploadFileController {

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    StudySampleDescPatchRequestAssembler studySampleDescPatchRequestAssembler;

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
        //List<StudyPatchRequest> studyPatchRequests = studyPatchRequestAssembler.disassemble(multipartFile);
        StudyPatchRequest studyPatchRequest = new StudyPatchRequest("", "", "");
        List<StudyPatchRequest>  studyPatchRequests =  (List<StudyPatchRequest>) fileHandler.disassemble(multipartFile, StudyPatchRequest.class, studyPatchRequest);
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
        //List<EfoTraitStudyMappingDto> efoTraitStudyMappingDtos = studyPatchRequestAssembler.disassembleForEfoMapping(multipartFile);
        EfoTraitStudyMappingDto efoTraitStudyMappingDto = new EfoTraitStudyMappingDto("","","");
        List<EfoTraitStudyMappingDto>  efoTraitStudyMappingDtos =  (List<EfoTraitStudyMappingDto>) fileHandler.disassemble(multipartFile, EfoTraitStudyMappingDto.class,efoTraitStudyMappingDto);
        List<TraitUploadReport> traitUploadReport = studiesService.updateEfoTraitsForStudies(efoTraitStudyMappingDtos, submissionId);
        byte[] result = fileHandler.serializePojoToTsv(traitUploadReport);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=studyTraitUploadReports.tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{submissionId}" + DepositionCurationConstants.API_STUDIES + DepositionCurationConstants.API_MULTI_TRAITS + "/files")
    public HttpEntity<UploadReportWrapper> uploadMultiTraitStudyMapping(@PathVariable String submissionId, @RequestParam MultipartFile multipartFile, HttpServletRequest request) {

        if(multipartFile.isEmpty()){
            throw new FileProcessingException("File not found");
        }
        userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        MultiTraitStudyMappingDto multiTraitStudyMappingDto = new MultiTraitStudyMappingDto("","","","");
        List<MultiTraitStudyMappingDto> multiTraitStudyMappingDtos = (List<MultiTraitStudyMappingDto>) fileHandler.disassemble(multipartFile, MultiTraitStudyMappingDto.class,  multiTraitStudyMappingDto);
        //List<MultiTraitStudyMappingDto> multiTraitStudyMappingDtos = studyPatchRequestAssembler.disassembleForMultiTraitMapping(multipartFile);
        UploadReportWrapper traitUploadReport = studiesService.updateMultiTraitsForStudies(multiTraitStudyMappingDtos, submissionId);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(traitUploadReport, responseHeaders);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}" + DepositionCurationConstants.API_STUDIES + DepositionCurationConstants.API_SAMPLEDESCRIPTION + "/files",
            produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HttpEntity<byte[]> getSampleDescriptions(@PathVariable String submissionId) {

        List<StudySampleDescPatchWrapper> sampleDescPatchRequests =
                Optional.ofNullable(studiesService.getStudies(submissionId, Pageable.unpaged()))
                .map(unpagedStudies -> unpagedStudies.stream().collect(Collectors.toList()))
                .map(studies -> studies.stream().map(studySampleDescPatchRequestAssembler::assembleWrapper)
                        .collect(Collectors.toList()))
                .orElse(null);
        byte[] result = fileHandler.serializePojoToTsv(sampleDescPatchRequests);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=studySampleDescriptions.tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);

    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{submissionId}" + DepositionCurationConstants.API_STUDIES + DepositionCurationConstants.API_SAMPLEDESCRIPTION + "/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HttpEntity<byte[]> uploadSampleDescriptions(@PathVariable String submissionId, @RequestParam MultipartFile multipartFile) {
        StudySampleDescPatchWrapper studySampleDescPatchWrapper = new StudySampleDescPatchWrapper("","","","");
        List<StudySampleDescPatchWrapper>  sampleDescPatchWrapperRequests =  (List<StudySampleDescPatchWrapper>) fileHandler.disassemble(multipartFile, StudySampleDescPatchWrapper.class,studySampleDescPatchWrapper);
        List<StudySampleDescPatchRequest> sampleDescPatchRequests = sampleDescPatchWrapperRequests.stream().map(studySampleDescPatchRequestAssembler::disassembleWrapper).collect(Collectors.toList());
        byte[] result = studiesService.uploadSampleDescriptions(sampleDescPatchRequests, submissionId);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=studySampleDescriptions_Extract");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

}
