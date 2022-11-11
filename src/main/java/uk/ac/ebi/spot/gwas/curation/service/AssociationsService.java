package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SnpStatusReportDto;

import java.util.List;

public interface AssociationsService {


    Association getAssociation(String associationId);

    List<Association> getAssociations(String submissionId);

    byte[] getSnpValidationReportTsv(String submissionId);

    SnpStatusReportDto getSnpStatus(String submissionId);

    void approveSnps(String submissionId);
}
