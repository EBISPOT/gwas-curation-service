package uk.ac.ebi.spot.gwas.curation.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.config.DiseaseTraitConfiguration;
import uk.ac.ebi.spot.gwas.curation.config.WebMvcConfig;
import uk.ac.ebi.spot.gwas.curation.config.security.AuthEntryPoint;
import uk.ac.ebi.spot.gwas.curation.config.security.JwtUtils;
import uk.ac.ebi.spot.gwas.curation.config.security.WebSecurityConfig;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.DiseaseTraitController;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.ProvenanceDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.config.SystemConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(DiseaseTraitController.class)
@Import(WebSecurityConfig.class)
public class DiseaseTraitControllerTest {


    @Autowired
    MockMvc mockMvc;

    @MockBean
    SystemConfigProperties systemConfigProperties;

    @MockBean
    AuthEntryPoint unauthorizedHandler;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    DiseaseTraitService diseaseTraitService;

    @MockBean
    DiseaseTraitRepository diseaseTraitRepository;

    @MockBean
    StudyRepository studyRepository;

    @MockBean
    UserService userService;

    @MockBean
    JWTService jwtService;

    @MockBean
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @MockBean
    DepositionCurationConfig depositionCurationConfig;

    @MockBean
    ProvenanceDtoAssembler provenanceDtoAssembler;



    User user;

    @Before
    public void setup() {
        when(systemConfigProperties.getServerName()).thenReturn("dummy");
        when(diseaseTraitService.getDiseaseTrait(any())).thenReturn(Optional.of(TestUtil.mockDiseaseTrait()));
        when(diseaseTraitDtoAssembler.toResource(any())).thenReturn(TestUtil.mockAssemblyResource());
        user = TestUtil.mockUserDetails();

    }

    @Test
    public void testGetDiseaseTrait() throws Exception{

        mockMvc.perform(get("/v1/reported-traits/1234").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

    }



}
