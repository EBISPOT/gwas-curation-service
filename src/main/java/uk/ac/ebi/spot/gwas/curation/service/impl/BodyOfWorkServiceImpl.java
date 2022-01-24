package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.BodyOfWorkRepository;
import uk.ac.ebi.spot.gwas.curation.service.BodyOfWorkService;
import uk.ac.ebi.spot.gwas.deposition.domain.BodyOfWork;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.Optional;

@Service
public class BodyOfWorkServiceImpl implements BodyOfWorkService {

    private static final Logger log = LoggerFactory.getLogger(BodyOfWorkService.class);

    @Autowired
    private BodyOfWorkRepository bodyOfWorkRepository;





    @Override
    public BodyOfWork retrieveBodyOfWork(String bodyOfWorkId) {
        log.info("[{}] Retrieving body of work: {}", bodyOfWorkId);
        Optional<BodyOfWork> optionalBodyOfWork;

            optionalBodyOfWork = bodyOfWorkRepository.findByBowIdAndArchived(bodyOfWorkId, false);

        if (!optionalBodyOfWork.isPresent()) {
            log.error("Unable to find body of work with ID: {}", bodyOfWorkId);
            throw new EntityNotFoundException("Unable to find body of work with ID: " + bodyOfWorkId);
        }

        log.info("Returning body of work: {}", optionalBodyOfWork.get().getBowId());
        return optionalBodyOfWork.get();
    }





}
