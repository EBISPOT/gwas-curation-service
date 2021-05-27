package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.gwas.curation.config.AssociationJaversServiceConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.AssociationRepository;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(AssociationJaversServiceConfiguration.class)
public class AssociationJaversServiceTest {
    @Autowired
    JaversCommonService javersCommonService;

    @Autowired
    AssociationsService associationsService;

    @Autowired
    AssociationJaversService associationJaversService;

    @MockBean
    AssociationRepository associationRepository;

    List<Association> oldAccsnsList;
    List<Association> newAccsnsList;

    @Before
    public void setup(){
        oldAccsnsList = TestUtil.getOldAssociations();
        newAccsnsList = TestUtil.getNewAssociations();
        when(associationRepository.findById(any())).thenReturn(null);
    }

    @Test
    public void testFindAssociationChanges() {
        Map<String,List<Association>> prevstudyAscnsMap = oldAccsnsList.stream()
                .collect(Collectors.groupingBy(Association::getStudyTag));
        Map<String,List<Association>> newstudyAscnsMap = newAccsnsList.stream()
                .collect(Collectors.groupingBy(Association::getStudyTag));
        VersionDiffStats diffStats = new VersionDiffStats();
        VersionDiffStats versionDiffStats = associationJaversService.findAssociationChanges("Maternal meta", prevstudyAscnsMap.get("Maternal meta"),
                newstudyAscnsMap.get("Maternal meta"), diffStats);
        assertNotNull(versionDiffStats);

    }

    @Test
    public void testGetAssociationVersionStats() {
        AddedRemoved addedRemoved = associationJaversService.getAssociationVersionStats(oldAccsnsList, newAccsnsList);
        assertNotNull(addedRemoved);
    }

}
