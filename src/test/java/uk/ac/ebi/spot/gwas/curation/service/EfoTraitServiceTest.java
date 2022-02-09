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
import uk.ac.ebi.spot.gwas.curation.config.EfoTraitConfiguration;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(EfoTraitConfiguration.class)
public class EfoTraitServiceTest {

    @Autowired
    EfoTraitService efoTraitService;

    @Autowired
    EfoTraitRepository efoTraitRepository;

    @Autowired
    StudyRepository studyRepository;

    User user;

    @Before
    public void setup() {

        when(efoTraitRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockEfoTrait()));
        when(efoTraitRepository.findByTraitContainingIgnoreCase(any(),any())).thenReturn(TestUtil.mockEfoTraitByTrait());
        when(efoTraitRepository.findAll()).thenReturn(TestUtil.mockEfoTraits().getContent());
        when(efoTraitRepository.findAll((Pageable) any())).thenReturn(TestUtil.mockEfoTraits());
        when(efoTraitRepository.save(any())).thenReturn(TestUtil.mockEfoTrait());
        doNothing().when(efoTraitRepository).deleteById(any());
        user = TestUtil.mockUserDetails();
    }


    @Test
    public void testGetEfoTrait() {
        Optional<EfoTrait> efoTraitOptional = efoTraitService.getEfoTrait("1cbced6789");
        assertNotNull(efoTraitOptional.get());
    }

    @Test
    public void testGetEfoTraits() {
        Pageable pageable = new PageRequest(0 , 10);
        Page<EfoTrait> traitPage = efoTraitService.getEfoTraits("cardiovascular disease", pageable);
        efoTraitService.getEfoTraits(null, pageable);
        assertNotNull(traitPage);
    }


    @Test
    public void testSaveEfoTrait() {
        EfoTraitDto efoTraitDto = TestUtil.mockEfoTraitDto();
        EfoTrait efoTrait = efoTraitService.fullyUpdateEfoTrait("1234", efoTraitDto, user);
        assertNotNull(efoTrait);
    }
}
