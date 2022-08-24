package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.AssociationsService;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;

import java.util.Objects;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class AssociationController {

    @Autowired
    AssociationsService associationsService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}" + DepositionCurationConstants.API_ASSOCIATIONS + "/snp-validation-report")
    public HttpEntity<byte[]> getSnpValidationReport(@PathVariable String submissionId) {
        byte[] result = Objects.requireNonNull(associationsService.getSnpValidationReportTsv(submissionId));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=snp-validation-report.tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}" + DepositionCurationConstants.API_ASSOCIATIONS + "/no-valid-snps")
    public HttpEntity<Integer> getNumberOfValidSnps(@PathVariable String submissionId) {
        return new HttpEntity<>(associationsService.getNumberOfValidSnps(submissionId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{submissionId}" + DepositionCurationConstants.API_ASSOCIATIONS + "/approve-snps")
    public void approveSnps(@PathVariable String submissionId) {
        associationsService.approveSnps(submissionId);
    }
}
