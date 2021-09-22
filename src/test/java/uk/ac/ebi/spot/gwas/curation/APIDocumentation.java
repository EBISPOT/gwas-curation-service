
package uk.ac.ebi.spot.gwas.curation;

import org.javers.core.Javers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.config.DiseaseTraitConfiguration;
import uk.ac.ebi.spot.gwas.curation.config.JettyConfig;
import uk.ac.ebi.spot.gwas.curation.config.MongoConfig;
import uk.ac.ebi.spot.gwas.curation.config.security.AuthEntryPoint;
import uk.ac.ebi.spot.gwas.curation.config.security.JwtUtils;
import uk.ac.ebi.spot.gwas.curation.config.security.WebSecurityConfig;
import uk.ac.ebi.spot.gwas.curation.repository.*;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.ProvenanceDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.curation.util.TestUtil;
import uk.ac.ebi.spot.gwas.deposition.config.SystemConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.domain.User;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import({WebSecurityConfig.class})
public class APIDocumentation {

    @Rule
    public final JUnitRestDocumentation
            restDocumentation = new JUnitRestDocumentation("src/main/asciidoc/generated-snippets");

    private RestDocumentationResultHandler restDocumentationResultHandler;

    @Value("${server.servlet.context-path:/curation-traits}")
    private String contextPath;

    @MockBean
    SystemConfigProperties systemConfigProperties;

    @MockBean
    AuthEntryPoint unauthorizedHandler;

    @MockBean
    EditFileUploadService editFileUploadService;

    @MockBean
    SubmissionService submissionService;

    @MockBean
    Javers javers;

    @MockBean
    AssociationsService associationsService;

    @MockBean
    StudiesService studiesService;

    @MockBean
    SamplesService samplesService;


    @MockBean
    SubmissionDiffService submissionDiffService;

    @MockBean
    ConversionJaversService conversionService;

    @MockBean
    CuratorAuthService curatorAuthService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    FileUploadJaversService fileUploadJaversService;

    @MockBean
    FileUploadsService fileUploadsService;

    @MockBean
    AuthTokenRepository authTokenRepository;

    @MockBean
    AssociationRepository associationRepository;

    @MockBean
    CuratorWhitelistRepository curatorWhitelistRepository;

    @MockBean
    FileUploadRepository fileUploadRepository;

    @MockBean
    NoteRepository noteRepository;

    @MockBean
    SampleRepository sampleRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    SubmissionRepository submissionRepository;

    @MockBean
    MongoTemplate mongoTemplate;

    @MockBean
    JettyConfig jettyConfig;

    @MockBean
    MongoConfig.MongoConfigDev mongoConfigDev;

    @MockBean
    MongoConfig.MongoConfigProd mongoConfigProd;

    @MockBean
    MongoConfig.MongoConfigSandbox mongoConfigSandbox;

    @MockBean
    MongoConfig.MongoConfiGCPSandbox mongoConfiGCPSandbox;


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

    @MockBean
    PagedResourcesAssembler assembler;






    User user;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    protected final ResponseFieldsSnippet pagingFields = responseFields(
            fieldWithPath("first").optional().description("Whether this is the first page of results"),
            fieldWithPath("last").optional().description("Whether this is the last page of results"),
            fieldWithPath("totalPages").optional().description("Total number of pages available for this result"),
            fieldWithPath("totalElements").optional().description("Total number of elements for this result"),
            fieldWithPath("size").optional().description("Maximum page size"),
            fieldWithPath("number").optional().description("Current page number"),
            fieldWithPath("numberOfElements").optional().description("Number of elements on this page"),
            fieldWithPath("sort").optional().description("Sort order of the page"));

    @Before
    public void setUp() {
        this.restDocumentationResultHandler = document("{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
        );

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation).uris()
                        .withScheme("http")
                        .withHost("193.62.54.159")
                        .withPort(80))

                .alwaysDo(this.restDocumentationResultHandler)
                .build();

        when(diseaseTraitRepository.findById(any())).thenReturn(Optional.of(TestUtil.mockDiseaseTrait()));
        when(diseaseTraitRepository.findByStudyIdsContainsAndTrait(any(),any(),any())).thenReturn(TestUtil.mockDiseaseTraits());
        when(diseaseTraitRepository.findByStudyIdsContains(any(),any())).thenReturn(TestUtil.mockDiseaseTraitByStudyId());
        when(diseaseTraitRepository.findByTrait(any(),any())).thenReturn(TestUtil.mockDiseaseTraitByTrait());
        when(diseaseTraitRepository.findAll()).thenReturn(TestUtil.mockDiseaseTraits().getContent());
        when(diseaseTraitRepository.save(any())).thenReturn(TestUtil.mockDiseaseTrait());
        user = TestUtil.mockUserDetails();

        when(systemConfigProperties.getServerName()).thenReturn("dummy");
        when(systemConfigProperties.getServerPort()).thenReturn("8080");
        when(systemConfigProperties.getServerName()).thenReturn("dummy");
        when(diseaseTraitService.getDiseaseTrait(any())).thenReturn(Optional.of(TestUtil.mockDiseaseTrait()));
        when(diseaseTraitService.getDiseaseTraits( any(), any(), any())).thenReturn((TestUtil.mockDiseaseTraits()));
        when(diseaseTraitDtoAssembler.toResource(any())).thenReturn(TestUtil.mockAssemblyResource());
    }


    @Test
    public void apiExample () throws Exception {
        this.mockMvc.perform(get(contextPath.concat("")).contextPath(contextPath.concat("")).accept(MediaTypes.HAL_JSON)
                .header("Authorization","Bearer SpringRestDocsDummyToken"))
                .andDo(this.restDocumentationResultHandler.document(
                        responseFields(
                                fieldWithPath("_links").description("<<Depo Curation>> to other resources")
                        ),
                        links(halLinks(),
                                linkWithRel("diseaseTraits").description("Link to all the Reported traits in the GWAS Catalog")
                        )))
                .andExpect(status().isOk());
    }


}

