package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.AssociationRepository;
import uk.ac.ebi.spot.gwas.curation.service.AssociationsService;
import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;


import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AssociationsServiceImpl implements AssociationsService {

    private static final Logger log = LoggerFactory.getLogger(AssociationsService.class);

    @Autowired
    private AssociationRepository associationRepository;



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



}
