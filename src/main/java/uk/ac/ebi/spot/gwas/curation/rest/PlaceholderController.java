package uk.ac.ebi.spot.gwas.curation.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.PlaceholderService;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_TEST)
public class PlaceholderController {

    private static final Logger log = LoggerFactory.getLogger(PlaceholderController.class);

    @Autowired
    private PlaceholderService placeholderService;

    /**
     * GET /v1/test
     */
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String test() {
        log.info("Request to call test.");
        return placeholderService.test();
    }

}
