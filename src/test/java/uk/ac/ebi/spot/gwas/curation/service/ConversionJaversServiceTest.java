package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.gwas.curation.config.ConversionJaversServiceConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.AssociationRepository;
import uk.ac.ebi.spot.gwas.curation.repository.FileUploadRepository;
import uk.ac.ebi.spot.gwas.curation.repository.SampleRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionSummary;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(ConversionJaversServiceConfiguration.class)
public class ConversionJaversServiceTest {

    @Autowired
    JaversCommonService javersCommonService;

    @Autowired
    private StudyJaversService studyJaversService;

    @Autowired
    StudiesService studiesService;

    @Autowired
    SampleJaversService sampleJaversService;

    @Autowired
    SamplesService samplesService;

    @Autowired
    AssociationJaversService associationJaversService;

    @Autowired
    AssociationsService associationsService;

    @Autowired
    private FileUploadJaversService fileUploadJaversService;

    @Autowired
    ConversionJaversService conversionJaversService;

    @Autowired
    FileUploadsService fileUploadsService;

    @MockBean
    SampleRepository sampleRepository;

    @MockBean
    StudyRepository studyRepository;

    @MockBean
    AssociationRepository associationRepository;

    @MockBean
    FileUploadRepository fileUploadRepository;

    List<JaversChangeWrapper> changesList;

    Optional<Map<Double, List<JaversChangeWrapper>>> changeMap;

    List<JaversChangeWrapper> changes1;
    List<JaversChangeWrapper> changes2;

    @Before
    public void setup() {
        changesList = TestUtil.mockJaversChangeWrapper();
        when(studyRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockStudyforProcessTag()));
        when(associationRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockAssociationForProcessTag()));
        when(sampleRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockSampleForProcessTag()));
        changeMap = conversionJaversService.filterJaversResponse(changesList);
    }

    @Test
    public void testFilterJaversResponse() {
        changeMap = conversionJaversService.filterJaversResponse(changesList);
        assertNotNull(changeMap);
    }

    @Test
    public void testCompareVersions() {
        List<VersionSummary> versions = conversionJaversService.buiildVersionSummary(changeMap);
        assertNotNull(versions);
    }



}
