package uk.ac.ebi.spot.gwas.curation.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;

import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionSummary;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionDiffService;
import uk.ac.ebi.spot.gwas.curation.service.ConversionJaversService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class SubmissionDiffController {

    private static final Logger log = LoggerFactory.getLogger(SubmissionDiffController.class);
    @Autowired
    SubmissionDiffService submissionDiffService;

    @Autowired
    ConversionJaversService conversionService;

    @GetMapping(
            value = "/{submissionId}" + DepositionCurationConstants.API_SUBMISSION_VERSION,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<VersionSummary> diffVersionSubmissions(@PathVariable String submissionId, HttpServletRequest request) {
        String jwtToken = CurationUtil.parseJwt(request);
        List<VersionSummary> summaries = null;
        ResponseEntity<List<JaversChangeWrapper>> responseEntity = submissionDiffService.diffVersionsSubmission(submissionId, jwtToken );
        List<JaversChangeWrapper> changeList = responseEntity.getBody();
        if(changeList != null && !changeList.isEmpty()) {
            Optional<Map<Double, List<JaversChangeWrapper>>> convertedEntityOptional = conversionService.filterJaversResponse(changeList);
            List<Double> versionMapTobeDeleted = conversionService.removeInvalidSumstatsEntries(changeList);
            if(versionMapTobeDeleted != null && !versionMapTobeDeleted.isEmpty())
                conversionService.removeVersionMap(convertedEntityOptional, versionMapTobeDeleted );
            summaries = conversionService.filterStudiesFromJavers(convertedEntityOptional);
            Optional<List<FileUpload>> fileUploadsOptional = conversionService.filterJaversResponseForFiles(responseEntity.getBody());
            summaries = fileUploadsOptional.isPresent()?conversionService.mapFilesToVersionSummary(summaries, fileUploadsOptional.get()):null;
        }
        return summaries;


    }
}
