package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.AssociationsService;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SnpStatusReportDto;

import java.util.Objects;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_SUBMISSIONS)
public class AssociationController {

    @Autowired
    AssociationsService associationsService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}" + DepositionCurationConstants.API_ASSOCIATIONS + "/snp-validation-report")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public HttpEntity<byte[]> getSnpValidationReport(@PathVariable String submissionId) {
        byte[] result = Objects.requireNonNull(associationsService.getSnpValidationReportTsv(submissionId));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=snp-validation-report.tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{submissionId}" + DepositionCurationConstants.API_ASSOCIATIONS + "/snp-status")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public HttpEntity<SnpStatusReportDto> getSnpStatus(@PathVariable String submissionId) {
        return new HttpEntity<>(associationsService.getSnpStatus(submissionId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{submissionId}" + DepositionCurationConstants.API_ASSOCIATIONS + "/approve-snps")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public void approveSnps(@PathVariable String submissionId) {
        associationsService.approveSnps(submissionId);
    }
}
