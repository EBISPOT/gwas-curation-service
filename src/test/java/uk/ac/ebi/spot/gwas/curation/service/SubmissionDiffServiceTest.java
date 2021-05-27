package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.config.SubmissionDiffServiceConfiguration;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(SubmissionDiffServiceConfiguration.class)
public class SubmissionDiffServiceTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    SubmissionDiffService submissionDiffService;

    @MockBean
    private RestInteractionConfig restInteractionConfig;


    @Test
    public void testDiffVersionsSubmission() {

        String submissionId = "test";
        String jwtToken = "test";
        when(restInteractionConfig.getCurationServiceUrl()).thenReturn("dummy");
        when(restInteractionConfig.getJaversEndpoint()).thenReturn("dummy");
        List<JaversChangeWrapper> mockResponse = TestUtil.mockJaversChangeWrapper();
        ResponseEntity<List<JaversChangeWrapper>> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<JaversChangeWrapper> >() {
        }))).thenReturn(responseEntity);
        ResponseEntity<List<JaversChangeWrapper>> actual = submissionDiffService.diffVersionsSubmission(submissionId, jwtToken);
        assertEquals(responseEntity, responseEntity);

    }
}
