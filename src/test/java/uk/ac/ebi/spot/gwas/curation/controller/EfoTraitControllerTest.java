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
import uk.ac.ebi.spot.gwas.curation.config.security.AuthEntryPoint;
import uk.ac.ebi.spot.gwas.curation.config.security.JwtUtils;
import uk.ac.ebi.spot.gwas.curation.config.security.WebSecurityConfig;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.EfoTraitController;
import uk.ac.ebi.spot.gwas.curation.rest.dto.EfoTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.ProvenanceDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.config.SystemConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.domain.User;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EfoTraitController.class)
@Import(WebSecurityConfig.class)
public class EfoTraitControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SystemConfigProperties systemConfigProperties;

    @MockBean
    AuthEntryPoint unauthorizedHandler;

    @MockBean
    EfoTraitService efoTraitService;

    @MockBean
    EfoTraitRepository efoTraitRepository;

    @MockBean
    StudyRepository studyRepository;

    @MockBean
    UserService userService;

    @MockBean
    JWTService jwtService;

    @MockBean
    EfoTraitDtoAssembler efoTraitDtoAssembler;

    @MockBean
    DepositionCurationConfig depositionCurationConfig;

    @MockBean
    ProvenanceDtoAssembler provenanceDtoAssembler;

    User user;

    @Before
    public void setup() {

        when(systemConfigProperties.getServerName()).thenReturn("dummy");
        when(efoTraitService.getEfoTrait(any())).thenReturn(Optional.of(TestUtil.mockEfoTrait()));
        when(efoTraitDtoAssembler.toResource(any())).thenReturn(TestUtil.mockEfoTraitAssemblyResource());
        user = TestUtil.mockUserDetails();
    }

    @Test
    public void testGetEfoTrait() throws Exception {

        mockMvc.perform(
                get("/v1/efo-traits/1234").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
