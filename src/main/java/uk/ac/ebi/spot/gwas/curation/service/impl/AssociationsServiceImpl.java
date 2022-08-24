package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.AssociationRepository;
import uk.ac.ebi.spot.gwas.curation.service.AssociationsService;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SnpValidationReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssociationsServiceImpl implements AssociationsService {

    private static final Logger log = LoggerFactory.getLogger(AssociationsService.class);

    @Autowired
    private AssociationRepository associationRepository;

    @Autowired
    private FileHandler fileHandler;



    @Override
    public Association getAssociation(String associationId) {
        log.info("Retrieving association: {}", associationId);
        Optional<Association> associationOptional = associationRepository.findById(associationId);
        if (associationOptional.isPresent()) {
            log.info("Found association: {}", associationOptional.get().getStudyTag());
            return associationOptional.get();
        }
        log.error("Unable to find association: {}", associationId);
        return null;
    }

    @Override
    public List<Association> getAssociations(String submissionId) {
        return associationRepository.readBySubmissionId(submissionId).collect(Collectors.toList());
    }

    @Override
    public byte[] getSnpValidationReportTsv(String submissionId) {
        List<Association> associations = getAssociations(submissionId);
        List<SnpValidationReport> snpValidationReports = new ArrayList<>();
        for (Association association: associations) {
            if (association.getValid() == null || !association.getValid()) {
                SnpValidationReport snpValidationReport = new SnpValidationReport(association.getVariantId(), "Not found in Ensembl");
                snpValidationReports.add(snpValidationReport);
            }
        }
        return fileHandler.serializePojoToTsv(snpValidationReports);
    }

    @Override
    public Integer getNumberOfValidSnps(String submissionId) {
        return associationRepository.countByIsValidAndSubmissionId(true, submissionId);
    }

    @Override
    public void approveSnps(String submissionId) {
        associationRepository.readBySubmissionId(submissionId).parallel().forEach(association -> {
            association.setValid(true);
            associationRepository.save(association);
        });
    }


}
