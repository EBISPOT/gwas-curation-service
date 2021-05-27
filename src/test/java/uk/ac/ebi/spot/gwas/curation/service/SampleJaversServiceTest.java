package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.gwas.curation.config.SampleJaversServiceConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.SampleRepository;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Sample;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@Import(SampleJaversServiceConfiguration.class)
public class SampleJaversServiceTest {

    @Autowired
    SampleJaversService sampleJaversService;

    @Autowired
    SamplesService samplesService;

    @MockBean
    SampleRepository sampleRepository;

    List<Sample> oldSamples;
    List<Sample> newSamples;

    @Before
    public void setup() {
        oldSamples = TestUtil.getOldSamples();
        newSamples = TestUtil.getNewSamples();
    }

    @Test
    public void testFindSampleChanges() {
        Map<String,List<Sample>> prevStudySamplesMap = oldSamples.stream()
                .collect(Collectors.groupingBy(Sample::getStudyTag));
        Map<String,List<Sample>> newStudySamplesMap = newSamples.stream()
                .collect(Collectors.groupingBy(Sample::getStudyTag));
        VersionDiffStats versionDiffStats = new VersionDiffStats();
        VersionDiffStats diffStats = sampleJaversService.findSampleChanges(prevStudySamplesMap.get("Maternal meta"), newStudySamplesMap.get("Maternal meta"),
                versionDiffStats);
        assertNotNull(diffStats);
    }

    @Test
    public void testGetSampleVersionStats() {
        AddedRemoved addedRemoved = sampleJaversService.getSampleVersionStats(oldSamples, newSamples );
        assertNotNull(addedRemoved);
    }

}
