package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.service.PlaceholderService;

@Service
public class PlaceholderServiceImpl implements PlaceholderService {

    private static final Logger log = LoggerFactory.getLogger(PlaceholderService.class);

    @Override
    public String test() {
        log.info("Request to return TEST.");
        return "TEST";
    }
}
