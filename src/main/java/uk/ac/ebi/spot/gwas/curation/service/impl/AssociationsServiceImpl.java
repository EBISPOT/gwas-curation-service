package uk.ac.ebi.spot.gwas.curation.service.impl;

import com.mongodb.bulk.BulkWriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.AssociationRepository;
import uk.ac.ebi.spot.gwas.curation.service.AssociationsService;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SnpStatusReportDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SnpValidationReport;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class AssociationsServiceImpl implements AssociationsService {

    private static final Logger log = LoggerFactory.getLogger(AssociationsService.class);

    @Autowired
    private AssociationRepository associationRepository;

    @Autowired
    private FileHandler fileHandler;

    @Autowired
    private MongoTemplate mongoTemplate;



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
        Map<String, SnpValidationReport> snpValidationReportMap = new HashMap<>();
        for (Association association: associations) {
            if (!association.getValid()) {
                SnpValidationReport snpValidationReport;
                if (snpValidationReportMap.containsKey(association.getVariantId())) {
                    snpValidationReport = new SnpValidationReport(association.getVariantId(), "Duplicate SNP in study");
                }
                else {
                    snpValidationReport = new SnpValidationReport(association.getVariantId(), "Not found in Ensembl");
                }
                snpValidationReportMap.put(association.getVariantId(), snpValidationReport);
            }
        }
        return fileHandler.serializePojoToTsv(new ArrayList<>(snpValidationReportMap.values()));
    }

    @Override
    public void approveSnps(String submissionId) {
        log.info("Started approving SNPs for submission: {}", submissionId);
        AtomicBoolean found = new AtomicBoolean(false);
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Association.class);
        associationRepository.readBySubmissionId(submissionId).parallel().forEach(association -> {
            Query query = new Query().addCriteria(new Criteria("id").is(association.getId()));
            Update update = new Update().set("isApproved", true);
            bulkOps.updateOne(query, update);
            found.set(true);
        });
        BulkWriteResult bulkWriteResult = null;
        if (found.get()) {
            bulkWriteResult = bulkOps.execute();
        }
        if (bulkWriteResult != null && bulkWriteResult.wasAcknowledged()) {
            log.info("Finished approving SNPs for submission: {}", submissionId);
        }
    }

    @Override
    public SnpStatusReportDto getSnpStatus(String submissionId) {
        SnpStatusReportDto snpStatusReportDto = new SnpStatusReportDto();
        snpStatusReportDto.setNoApprovedSnps(associationRepository.countByIsApprovedAndSubmissionId(true, submissionId));
        snpStatusReportDto.setNoValidSnps(associationRepository.countByIsValidAndSubmissionId(true, submissionId));
        return snpStatusReportDto;
    }
}
