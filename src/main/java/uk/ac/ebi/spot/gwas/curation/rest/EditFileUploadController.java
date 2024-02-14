package uk.ac.ebi.spot.gwas.curation.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;
import uk.ac.ebi.spot.gwas.deposition.dto.SubmissionDto;
import uk.ac.ebi.spot.gwas.deposition.rest.RestRequestUtil;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class EditFileUploadController {

    private static final Logger log = LoggerFactory.getLogger(EditFileUploadController.class);

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RestInteractionConfig restInteractionConfig;

    @Autowired
    protected RestRequestUtil restRequestUtil;

    @Autowired
    EditFileUploadService editFileUploadService;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserService userService;

    @Autowired
    SubmissionService submissionService;

    @Autowired
    StudiesService studiesService;
    /*
     * POST /v1/submissions/{submissionId}/edituploads
     */
    @PostMapping(
            value = "/{submissionId}" + DepositionCurationConstants.API_EDIT_UPLOADS,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Resource<FileUploadDto> uploadEditFile(@RequestParam MultipartFile file,
                                                  @PathVariable String submissionId,
                                                  HttpServletRequest request)  {

        String jwtToken = CurationUtil.parseJwt(request);
        ResponseEntity<Resource<FileUploadDto>> fileUploadDtoResource = editFileUploadService.uploadEditFIle(jwtToken, submissionId, file );
        studiesService.sendMetaDataMessageToQueue(submissionId);
        return fileUploadDtoResource.getBody();
    }

    /*
     * PUT /v1/submissions/{submissionId}/lock?lockStatus=lock|unlock
     */
    @PutMapping(value = "/{submissionId}" + DepositionCurationConstants.API_SUBMISSIONS_LOCK,
            produces = MediaTypes.HAL_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @ResponseStatus(HttpStatus.OK)
    public Resource<SubmissionDto> lockSubmission(@RequestParam String lockStatus, @PathVariable String submissionId, HttpServletRequest request) {


        String jwtToken = CurationUtil.parseJwt(request);
        ResponseEntity<Resource<SubmissionDto>> submissionDtoResource = editFileUploadService.lockSubmission(jwtToken, lockStatus , submissionId );
        return submissionDtoResource.getBody();

    }

}
