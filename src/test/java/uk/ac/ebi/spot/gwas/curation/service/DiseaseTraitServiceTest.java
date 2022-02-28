package uk.ac.ebi.spot.gwas.curation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.gwas.curation.config.DiseaseTraitConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.service.impl.DiseaseTraitServiceImpl;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(DiseaseTraitConfiguration.class)
public class DiseaseTraitServiceTest {

    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    DiseaseTraitRepository diseaseTraitRepository;

    @Autowired
    StudyRepository studyRepository;

    User user;

    @Before
    public void setup() {
        when(diseaseTraitRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockDiseaseTrait()));
        when(diseaseTraitRepository.findByTrait(any(),any())).thenReturn(TestUtil.mockDiseaseTraitByTrait());
        when(diseaseTraitRepository.findAll()).thenReturn(TestUtil.mockDiseaseTraits().getContent());
        when(diseaseTraitRepository.save(any())).thenReturn(TestUtil.mockDiseaseTrait());
        user = TestUtil.mockUserDetails();

    }


  @Test
  public void testGetDiseaseTrait() {
      Optional<DiseaseTrait> diseaseTraitOpt = diseaseTraitService.getDiseaseTrait("1cbced6789");
      assertNotNull(diseaseTraitOpt.get());
  }

  @Test
  public void testGetDiseaseTraits() {
      Pageable pageable = new PageRequest(0 , 10);
      Page<DiseaseTrait> traitPage = diseaseTraitService.getDiseaseTraits("wg rh intensity-contrast paracentral","study1", pageable);
      diseaseTraitService.getDiseaseTraits("wg rh intensity-contrast paracentral",null, pageable);
      diseaseTraitService.getDiseaseTraits(null,"study1", pageable);
      diseaseTraitService.getDiseaseTraits(null, null ,pageable);
      assertNotNull(traitPage);
  }


  @Test
  public void testSaveDiseaseTrait() {
      DiseaseTraitDto diseaseTraitDto = TestUtil.mockDiseaseTraitDto();
      DiseaseTrait diseaseTrait = diseaseTraitService.saveDiseaseTrait("1234", diseaseTraitDto, user );
      assertNotNull(diseaseTrait);
  }
}
