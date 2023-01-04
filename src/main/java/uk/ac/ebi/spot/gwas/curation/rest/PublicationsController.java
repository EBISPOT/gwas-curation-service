package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationStatusReport;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_PUBLICATIONS)
public class PublicationsController {

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    PublicationService publicationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{pmids}",produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PublicationStatusReport> createPublication(@PathVariable List<String> pmids, HttpServletRequest request ) {
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        return publicationService.createPublication(pmids, user);
    }

}
