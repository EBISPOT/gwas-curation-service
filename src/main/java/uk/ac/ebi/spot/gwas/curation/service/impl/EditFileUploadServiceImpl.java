package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.EditFileUploadService;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;
import uk.ac.ebi.spot.gwas.deposition.dto.SubmissionDto;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.IOException;

@Service
public class EditFileUploadServiceImpl implements EditFileUploadService {

    private static final Logger log = LoggerFactory.getLogger(EditFileUploadServiceImpl.class);

    @Autowired
    RestInteractionConfig restInteractionConfig;

    @Autowired
    @Qualifier("restTemplateCuration")
    RestTemplate restTemplate;

    /**
     * Make A rest Template call to upload the file in Deposition File Upload
     * @param jwtToken
     * @param submissionId
     * @param file
     * @return
     */
    public ResponseEntity<Resource<FileUploadDto>> uploadEditFIle(String jwtToken, String submissionId, MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(jwtToken);
            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition.builder("form-data")
                    .name("file")
                    .filename(file.getOriginalFilename())
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);
            body.add("file", fileEntity);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
            String endpoint = restInteractionConfig.getDepositionServiceUrl() + submissionId + DepositionCurationConstants.API_EDIT_UPLOADS;
            log.info("Rest Template call "+endpoint);
            ResponseEntity<Resource<FileUploadDto>> fileUploadDtoResource = restTemplate.exchange(endpoint,
                    HttpMethod.POST, httpEntity, new ParameterizedTypeReference<Resource<FileUploadDto>>() {
                    });
            return fileUploadDtoResource;
        } catch (IOException ex) {
            log.error("Unable to store file [{}]: {}", file.getOriginalFilename(), ex.getMessage(), ex);
        }
        throw new FileProcessingException("Unable to store file: " + file.getOriginalFilename());

    }


    public ResponseEntity<Resource<SubmissionDto>> lockSubmission(String jwtToken, String lockStatus, String submissionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String endpoint = restInteractionConfig.getDepositionServiceUrl() +  restInteractionConfig.getSubmissionEndpoint()+submissionId + DepositionCurationConstants.API_SUBMISSIONS_LOCK;
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(endpoint).queryParam("lockStatus", lockStatus).build();
        log.info("The Lock URL is"+uriComponents.toUriString());
        ResponseEntity<Resource<SubmissionDto>> resourceSubmission = restTemplate.exchange(uriComponents.toUriString(),HttpMethod.PUT, httpEntity, new ParameterizedTypeReference<Resource<SubmissionDto>>() {
        });
        return resourceSubmission;

    }


}
