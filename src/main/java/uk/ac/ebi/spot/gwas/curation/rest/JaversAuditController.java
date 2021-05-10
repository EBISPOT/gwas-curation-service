package uk.ac.ebi.spot.gwas.curation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.curation.util.HeadersUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.*;
import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;


import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = GeneralCommon.API_V1+ DepositionCurationConstants.API_JAVERS)
public class JaversAuditController {

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
    public JaversChangeWrapper getSubmissionChanges(@PathVariable  String submissionId, HttpServletRequest request) throws Exception {
        User user = userService.findUser(jwtService.extractUser(HeadersUtil.extractJWT(request)), false);
        Submission submission = submissionService.getSubmission(submissionId, user);
        QueryBuilder queryBuilder = QueryBuilder.byInstance(submission);
        Changes changes = javers.findChanges(queryBuilder.build());
        JaversChangeWrapper javersChangeWrapper = new ObjectMapper().readValue(
                javers.getJsonConverter().toJson(changes), JaversChangeWrapper.class);
        return javersChangeWrapper;

    }

    @GetMapping(value = "/association/{associationId}/changes",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String getAssociationChanges(@PathVariable  String associationId, HttpServletRequest request){
        User user = userService.findUser(jwtService.extractUser(HeadersUtil.extractJWT(request)), false);
        Association association = associationsService.getAssociation(associationId);
        QueryBuilder queryBuilder = QueryBuilder.byInstance(association);
        Changes changes = javers.findChanges(queryBuilder.build());
        
        return javers.getJsonConverter().toJson(changes);

    }


    @GetMapping(value = "/study/{studyId}/changes",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String getStudyChanges(@PathVariable  String studyId, HttpServletRequest request){
        User user = userService.findUser(jwtService.extractUser(HeadersUtil.extractJWT(request)), false);
        Study study = studiesService.getStudy(studyId);
        QueryBuilder queryBuilder = QueryBuilder.byInstance(study);
        Changes changes = javers.findChanges(queryBuilder.build());
        return javers.getJsonConverter().toJson(changes);
    }


    @GetMapping(value = "/sample/{sampleId}/changes",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String getSampleChanges(@PathVariable  String sampleId, HttpServletRequest request){
        User user = userService.findUser(jwtService.extractUser(HeadersUtil.extractJWT(request)), false);
        Sample sample = samplesService.getSample(sampleId);
        QueryBuilder queryBuilder = QueryBuilder.byInstance(sample);
        Changes changes = javers.findChanges(queryBuilder.build());
        return javers.getJsonConverter().toJson(changes);
    }


}


