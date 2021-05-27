
package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.gwas.curation.config.StudyJaversServiceConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(StudyJaversServiceConfiguration.class)
public class StudyJaversServiceTest {

    @Autowired
    private StudyJaversService studyJaversService;

    @Autowired
    private JaversCommonService javersCommonService;

    @MockBean
    StudyRepository studyRepository;



    List<Study> oldStudyList;
    List<Study> newStudyList;

    @Before
    public void setup(){
        oldStudyList = TestUtil.getOldStudies();
        newStudyList = TestUtil.getNewStudies();
        when(studyRepository.findById(any())).thenReturn(null);
    }

    @Test
    public void testFindStudyChanges(){
        Map<String, List<Study>> prevStudyMap = oldStudyList.stream()
                .collect(Collectors.groupingBy(Study::getStudyTag));
        VersionDiffStats versionDiffStats = studyJaversService.findStudyChanges("Maternal meta", prevStudyMap.get("Maternal meta"),
                newStudyList);
        assertNotNull(versionDiffStats);

    }

    @Test
    public void testGetStudyVersionStats() {
        VersionDiffStats versionDiffStats = new VersionDiffStats();
        AddedRemoved addedRemoved = studyJaversService.getStudyVersionStats(oldStudyList, newStudyList,  versionDiffStats);
        assertNotNull(addedRemoved);

    }

    @Test
    public void testGetReportedTraitVersionStats() {
        AddedRemoved addedRemoved = studyJaversService.getReportedTraitVersionStats(oldStudyList, newStudyList);
        assertNotNull(addedRemoved);
    }

    @Test
    public void testGetReportedEfoVersionStats() {
        AddedRemoved addedRemoved = studyJaversService.getReportedEfoVersionStats(oldStudyList, newStudyList);
        assertNotNull(addedRemoved);
    }


}

