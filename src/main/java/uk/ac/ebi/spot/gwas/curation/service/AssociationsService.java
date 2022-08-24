package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.Association;

import java.util.List;

public interface AssociationsService {


    Association getAssociation(String associationId);

    List<Association> getAssociations(String submissionId);

    byte[] getSnpValidationReportTsv(String submissionId);

    Integer getNumberOfValidSnps(String submissionId);

    void approveSnps(String submissionId);
}
