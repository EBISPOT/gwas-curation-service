
package uk.ac.ebi.spot.gwas.curation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
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
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.FileUploadRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import({WebSecurityConfig.class})
public class APIDocumentation {

    @Rule
    public final JUnitRestDocumentation
            restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

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

    @MockBean
    FileUploadRequest fileUploadRequest;




    private ObjectMapper mapper = new ObjectMapper();




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
                .alwaysDo(document("{method-name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ))
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
        when(userService.findUser(any(), anyBoolean())).thenReturn(user);
        when(diseaseTraitService.getDiseaseTrait(any())).thenReturn(Optional.of(TestUtil.mockDiseaseTrait()));
        when(diseaseTraitService.createDiseaseTrait(any())).thenReturn(TestUtil.mockDiseaseTrait());

        when(diseaseTraitService.saveDiseaseTrait(any(), any(), any())).thenReturn(TestUtil.mockDiseaseTrait());
        when(diseaseTraitService.getDiseaseTraits( any(), any(), any())).thenReturn((TestUtil.mockDiseaseTraits()));
        when(diseaseTraitDtoAssembler.toResource(any())).thenReturn(TestUtil.mockAssemblyResource());
        when(diseaseTraitDtoAssembler.disassemble(any(DiseaseTraitDto.class))).thenReturn(TestUtil.mockDiseaseTrait());


        doNothing().when(diseaseTraitService).deleteDiseaseTrait(any());
    }

    @Test
    public void pageExample () throws Exception {

        this.mockMvc.perform(get(contextPath.concat("/v1/reported-traits?page=1&size=1")).contextPath(contextPath.concat("")).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization","Bearer SpringRestDocsDummyToken"))
                .andDo(this.restDocumentationResultHandler.document(
                        responseFields(
                                subsectionWithPath("_links").description("<<resources-page-links,Links>> to other resources"),
                                subsectionWithPath("_embedded").description("The list of resources"),
                                subsectionWithPath("page.size").description("The number of resources in this page"),
                                subsectionWithPath("page.totalElements").description("The total number of resources"),
                                subsectionWithPath("page.totalPages").description("The total number of pages"),
                                subsectionWithPath("page.number").description("The page number")
                        ),
                        links(halLinks(),
                                linkWithRel("self").description("This resource list"),
                                linkWithRel("first").description("The first page in the resource list"),
                                linkWithRel("next").description("The next page in the resource list"),
                                linkWithRel("last").description("The last page in the resource list")
                        )

                ))
                .andExpect(status().isOk());
    }




    @Test
    public void diseaseTraitsListExample() throws Exception {
        this.mockMvc.perform(get(contextPath.concat("/v1/reported-traits?page=1&size=1&sort=trait&trait=dummy")).contextPath(contextPath.concat("")).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization","Bearer SpringRestDocsDummyToken"))
                .andExpect(status().isOk())
                .andDo(document("disease-traits-list-example",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(parameterWithName("trait").description("the trait to search"),
                                parameterWithName("page").description("The page number"),
                                parameterWithName("size").description("The size of elements in a page"),
                                parameterWithName("sort").description("the property to sort the records")),
                        links(halLinks(),
                                linkWithRel("self").description("This resource list"),
                                linkWithRel("first").description("The first page in the resource list"),
                                linkWithRel("next").description("The next page in the resource list"),
                                linkWithRel("last").description("The last page in the resource list")
                        )));
    }

    @Test
    public void getDiseaseTraitExample() throws Exception {
        this.mockMvc.perform(get(contextPath.concat("/v1/reported-traits/{traitId}"),"16510553").contextPath(contextPath.concat("")).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization","Bearer SpringRestDocsDummyToken"))
                .andExpect(status().isOk())
                .andDo(this.restDocumentationResultHandler.document(
                        pathParameters(
                                parameterWithName("traitId").description("The unique id of the reported trait in the GWAS Catalog")
                        ),
                        responseFields(
                                subsectionWithPath("_links").description("<<diseaseTraits-links,Links>> to other resources"),
                                fieldWithPath("diseaseTraitId").description("The unique id of the reported trait"),
                                fieldWithPath("trait").description("The name of the reported trait"),
                                fieldWithPath("studies").description("The study tags associated with trait"),
                                subsectionWithPath("created").description("The user details & timestamp of created date")
                        ),
                        links(halLinks(),
                                linkWithRel("self").description("This resource list")
                        )));

    }




    @Test
    public void addDiseaseTraitExample() throws Exception {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("trait", "dummyTrait");
        this.mockMvc.perform(post(contextPath.concat("/v1/reported-traits")).contextPath(contextPath.concat("")).accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(this.mapper.writeValueAsString(payloadMap))
                .header("Authorization", "Bearer SpringRestDocsDummyToken"))
                     .andExpect(status().isCreated())
                 .andDo(this.restDocumentationResultHandler.document(
                         requestFields(fieldWithPath("trait").description("The name of the reported trait")),
                         responseFields(
                                 subsectionWithPath("_links").description("<<diseaseTraits-links,Links>> to other resources"),
                                 fieldWithPath("diseaseTraitId").description("The unique id of the reported trait"),
                                 fieldWithPath("trait").description("The name of the reported trait"),
                                 fieldWithPath("studies").description("The study tags associated with trait"),
                                 subsectionWithPath("created").description("The user details & timestamp of created date")
                         ),
                         links(halLinks(),
                                 linkWithRel("self").description("This resource list")
                         )));
    }

    @Test
    public void updateDiseaseTraitExample() throws Exception {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("trait", "dummyTrait");
        this.mockMvc.perform(put(contextPath.concat("/v1/reported-traits/{traitId}"),"16510553").contextPath(contextPath.concat("")).accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(this.mapper.writeValueAsString(payloadMap))
                .header("Authorization", "Bearer SpringRestDocsDummyToken"))
                .andExpect(status().isOk())
                .andDo(this.restDocumentationResultHandler.document(
                        pathParameters(
                                parameterWithName("traitId").description("The unique id of the reported trait in the GWAS Catalog")
                        ),
                        requestFields(fieldWithPath("trait").description("The name of the reported trait")),
                        responseFields(
                                subsectionWithPath("_links").description("<<diseaseTraits-links,Links>> to other resources"),
                                fieldWithPath("diseaseTraitId").description("The unique id of the reported trait"),
                                fieldWithPath("trait").description("The name of the reported trait"),
                                fieldWithPath("studies").description("The study tags associated with trait"),
                                subsectionWithPath("created").description("The user details & timestamp of created date")
                        ),
                        links(halLinks(),
                                linkWithRel("self").description("This resource list")
                        )));
    }

    @Test
    public void deleteDiseaseTraitExample() throws Exception {
        this.mockMvc.perform(delete(contextPath.concat("/v1/reported-traits/{traitId}"),"16510553").contextPath(contextPath.concat(""))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer SpringRestDocsDummyToken"))
                .andExpect(status().isNoContent())
                .andDo(this.restDocumentationResultHandler.document(
                        pathParameters(
                                parameterWithName("traitId").description("The unique id of the reported trait in the GWAS Catalog")
                        )));
    }


}

