package uk.ac.ebi.spot.gwas.curation.rest;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;
import uk.ac.ebi.spot.gwas.deposition.exception.FileValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_DISEASE_TRAITS_FILE_UPLOAD)
public class DiseaseTraitFileUploadController {

    private static final Logger log = LoggerFactory.getLogger(DiseaseTraitFileUploadController.class);

    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Autowired
    FileHandler fileHandler;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HttpEntity<byte[]> uploadDiseaseTraits(@RequestParam MultipartFile multipartFile,
                                                                        HttpServletRequest request) {
        if(multipartFile.isEmpty()){
            throw new FileProcessingException("File not found");
        }
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        List<DiseaseTrait> diseaseTraits = diseaseTraitDtoAssembler.disassemble(multipartFile);
        List<TraitUploadReport> traitUploadReports = diseaseTraitService.createDiseaseTrait(diseaseTraits, user);
        byte[] result = fileHandler.serializePojoToTsv(traitUploadReports);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=traitUploadReports.tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource<AnalysisCacheDto>  similaritySearchAnalysis(@RequestParam MultipartFile multipartFile) throws IOException {
        if(multipartFile.isEmpty()){
            throw new FileProcessingException("File not found");
        }
        String analysisId = UUID.randomUUID().toString();
        List<AnalysisDTO> analysisDTOS = fileHandler.serializeDiseaseTraitAnalysisFile(multipartFile);
        log.info("{} disease traits were ingested for analysis", analysisDTOS.size());
        AnalysisCacheDto  analysisCacheDto = diseaseTraitService.similaritySearch(analysisDTOS, analysisId, 50);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(DiseaseTraitFileUploadController.class).similaritySearchAnalysisCsvDownload(analysisId));
        Resource<AnalysisCacheDto> resource = new Resource<>(analysisCacheDto);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withSelfRel());
        return resource;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value ="/analysis/{analysisId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public HttpEntity<byte[]> similaritySearchAnalysisCsvDownload(@PathVariable String analysisId)
            throws IOException{
        log.info("Retrieving Cached Analysis with ID  : {}", analysisId);

        double threshold = 50.0;
        List<AnalysisDTO> analysisDTO = new ArrayList<>();
        AnalysisCacheDto cache = diseaseTraitService.similaritySearch(analysisDTO, analysisId, threshold);
        List<AnalysisDTO> analysisDTOs = cache.getAnalysisResult();
        analysisDTOs.sort(Comparator.comparingDouble(AnalysisDTO::getDegree).reversed());
        byte[] result = fileHandler.serializePojoToTsv(analysisDTOs);
        //log.info(result);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analysis.csv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));

        return new HttpEntity<>(result, responseHeaders);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/templates", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public HttpEntity<byte[]> fileUploadTemplateDownload(HttpServletResponse response,
                                             @RequestParam(value = "file") String fileUploadType) throws IOException {

        byte[] result = fileHandler.getTemplate(fileUploadType).getBytes();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileUploadType+".tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

}
