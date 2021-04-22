package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.SampleRepository;
import uk.ac.ebi.spot.gwas.curation.service.SamplesService;
import uk.ac.ebi.spot.gwas.deposition.domain.Sample;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;


import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class SamplesServiceImpl implements SamplesService {

    private static final Logger log = LoggerFactory.getLogger(SamplesService.class);

    @Autowired
    private SampleRepository sampleRepository;



    @Override
    public Sample getSample(String sampleId) {
        log.info("Retrieving sample: {}", sampleId);
        Optional<Sample> sampleOptional = sampleRepository.findById(sampleId);
        if (sampleOptional.isPresent()) {
            log.info("Found sample: {}", sampleOptional.get().getStudyTag());
            return sampleOptional.get();
        }
        log.error("Unable to find sample: {}", sampleId);
        return null;
    }



}
