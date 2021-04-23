package uk.ac.ebi.spot.gwas.curation.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.constants.IDPConstants;
import uk.ac.ebi.spot.gwas.curation.service.EditFileUploadService;
import uk.ac.ebi.spot.gwas.curation.service.impl.EditFileUploadServiceImpl;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.HeadersUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;
import uk.ac.ebi.spot.gwas.deposition.rest.RestRequestUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                                                  HttpServletRequest request) throws IOException {

        String jwtToken = CurationUtil.parseJwt(request);
        log.info("Jwt token ->"+jwtToken);
        ResponseEntity<Resource<FileUploadDto>> fileUploadDtoResource = editFileUploadService.uploadEditFIle(jwtToken, submissionId, file );

       return fileUploadDtoResource.getBody();
    }
}
