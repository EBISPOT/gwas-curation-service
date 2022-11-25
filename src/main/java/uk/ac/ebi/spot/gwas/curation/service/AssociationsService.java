package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SnpStatusReportDto;

import java.util.List;
import java.util.stream.Stream;

public interface AssociationsService {


    Association getAssociation(String associationId);

    Stream<Association> readBySeqIds(List<String> ids);

    List<Association> getAssociations(String submissionId);

    byte[] getSnpValidationReportTsv(String submissionId);

    SnpStatusReportDto getSnpStatus(String submissionId);

    void approveSnps(String submissionId);
}
