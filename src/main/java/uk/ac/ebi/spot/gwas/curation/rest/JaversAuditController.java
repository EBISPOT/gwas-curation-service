package uk.ac.ebi.spot.gwas.curation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.repository.jql.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.*;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.curation.util.HeadersUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V1+ DepositionCurationConstants.API_JAVERS)
public class JaversAuditController {

    private static final Logger log = LoggerFactory.getLogger(JaversAuditController.class);

    private final Javers javers;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private AssociationsService associationsService;

    @Autowired
    private StudiesService studiesService;

    @Autowired
    private SamplesService samplesService;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    public JaversAuditController(Javers javers){
        this.javers = javers;
    }

/*
     * GET /v1/submissions/{submissionId}/changes

 */


    @GetMapping(value = "/submissions/{submissionId}/changes",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<JaversChangeWrapper> getSubmissionChanges(@PathVariable  String submissionId, HttpServletRequest request) throws Exception {
        User user = userService.findUser(jwtService.extractUser(HeadersUtil.extractJWT(request)), false);
        Submission submission = submissionService.getSubmission(submissionId, user);
        QueryBuilder queryBuilder = QueryBuilder.byInstance(submission);
        Changes changes = javers.findChanges(queryBuilder.build());
        try {
            JaversChangeWrapper[] javersChangeWrapperArray = new ObjectMapper().readValue(
                    javers.getJsonConverter().toJson(changes), JaversChangeWrapper[].class);
            return Arrays.asList(javersChangeWrapperArray);

        }catch(Exception ex){
            log.error("Error in mapping Javers Changes "+ex.getMessage(),ex);
            return null;
        }

    }


}



