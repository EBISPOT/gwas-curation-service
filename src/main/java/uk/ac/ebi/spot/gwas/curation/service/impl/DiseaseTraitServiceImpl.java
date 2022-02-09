package uk.ac.ebi.spot.gwas.curation.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotCreateTraitWithDuplicateNameException;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotDeleteTraitException;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DiseaseTraitServiceImpl implements DiseaseTraitService {

    private static final Logger log = LoggerFactory.getLogger(DiseaseTraitServiceImpl.class);

    private ObjectMapper mapper = new ObjectMapper();



    private DiseaseTraitRepository diseaseTraitRepository;

    @Autowired
    RestInteractionConfig restInteractionConfig;

    @Autowired
    @Qualifier("restTemplateCuration")
    RestTemplate restTemplate;

    @Autowired
    private StudyRepository studyRepository;

    public DiseaseTraitServiceImpl(DiseaseTraitRepository diseaseTraitRepository) {
        this.diseaseTraitRepository = diseaseTraitRepository;
    }

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait) {
        Optional<DiseaseTrait> optDiseaseTrait = getDiseaseTraitByTraitName(diseaseTrait.getTrait());
        if(!optDiseaseTrait.isPresent())
        return diseaseTraitRepository.insert(diseaseTrait);
        else
        throw new CannotCreateTraitWithDuplicateNameException("Trait already exists with name"+optDiseaseTrait.get().getTrait());
    }


    public void callOldCurationService(MultipartFile multipartFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition.builder("form-data")
                    .name("multipartFile")
                    .filename(multipartFile.getOriginalFilename())
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(multipartFile.getBytes(), fileMap);
            body.add("file", fileEntity);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
            String endpoint = restInteractionConfig.getOldCurationUrl() +  restInteractionConfig.getOldDiseaseTraitsUploadEndpoint();
            log.info("Rest Template call " + endpoint);
            ResponseEntity<Object> diseaseTraitDtos = restTemplate.exchange(endpoint,
                    HttpMethod.POST, httpEntity, new ParameterizedTypeReference<Object>() {
                    });
        } catch (IOException ex) {
            log.error("Unable to store file [{}]: {}", multipartFile.getOriginalFilename(), ex.getMessage(), ex);
        }

    }

    public String  callOldCurationServiceSearch(String trait){
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,false);
        HttpHeaders headers = new HttpHeaders();
        PagedResources<DiseaseTraitDto> traitDtosResources = null;
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        log.info("Trait name in callOldCurationServiceSearch -:"+trait);
        String endpoint = restInteractionConfig.getOldCurationUrl() +  restInteractionConfig.getOldDiseaseTraitsSearchEndpoint();
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(endpoint).queryParam("query", trait).build();
        log.info("Rest Template call " + uriComponents.toUriString());
        ResponseEntity<Object> diseaseTraitDtos = restTemplate.exchange(uriComponents.toUriString(),
                HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Object>() {
                });
        try {
            log.info("diseaseTraitDtos as Json -:" + mapper.writeValueAsString(diseaseTraitDtos.getBody()));
            String jsonBody = mapper.writeValueAsString(diseaseTraitDtos.getBody());
            JsonNode jsonNode = mapper.readValue(jsonBody, JsonNode.class);
            JsonNode traitNode = jsonNode.get("_embedded");
            JsonNode diseaseTraits = traitNode.get("diseaseTraits");
            JsonNode diseaseTrait = diseaseTraits.get(0);
            JsonNode idNode = diseaseTrait.get("id");
            String id = idNode.asText();
            log.info("trait id from curation -:"+id);
            return id;
        } catch(JsonProcessingException ex){
        log.error("Exceptionn in processing",ex.getMessage(),ex);
        }
        catch(IOException ex){
            log.error("Exceptionn in processing",ex.getMessage(),ex);
        }
    return null;
}


    public void callOldCurationServiceDelete(String traitName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        log.info("Trait name is"+traitName);
        String traitId = callOldCurationServiceSearch(traitName);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        if(traitId != null) {
            String endpoint = restInteractionConfig.getOldCurationUrl() + restInteractionConfig.getOldDiseaseTraitsEndpoint() + "/" + traitId;
            log.info("Rest Template call " + endpoint);
            ResponseEntity<String> deleteMessage = restTemplate.exchange(endpoint,
                    HttpMethod.DELETE, httpEntity, String.class);
        }
    }


    public void callOldCurationServiceInsert(DiseaseTraitDto diseaseTraitDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        DiseaseTraitWrapperDTO   diseaseTraitWrapperDTO = new DiseaseTraitWrapperDTO(diseaseTraitDto.getTrait());
        HttpEntity<DiseaseTraitWrapperDTO> httpEntity = new HttpEntity<>(diseaseTraitWrapperDTO, headers);
        String endpoint = restInteractionConfig.getOldCurationUrl() + restInteractionConfig.getOldDiseaseTraitsEndpoint();
        log.info("Trait from request"+diseaseTraitDto.getTrait());
        log.info("Rest Template call " + endpoint);
        ResponseEntity<DiseaseTraitDto> entity = restTemplate.exchange(endpoint,
                HttpMethod.POST, httpEntity, DiseaseTraitDto.class);

    }

    public void callOldCurationServiceUpdate(DiseaseTraitDto diseaseTraitDto,String traitName) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        DiseaseTraitWrapperDTO  diseaseTraitWrapperDTO = new DiseaseTraitWrapperDTO(diseaseTraitDto.getTrait());
        HttpEntity<DiseaseTraitWrapperDTO> httpEntity = new HttpEntity<>(diseaseTraitWrapperDTO, headers);
        log.info("traitName in callOldCurationServiceUpdate -: " + traitName);
        String traitIdCurationId = callOldCurationServiceSearch(traitName);
        if(traitIdCurationId != null) {
            String endpoint = restInteractionConfig.getOldCurationUrl() + restInteractionConfig.getOldDiseaseTraitsEndpoint() + "/" + traitIdCurationId;
            log.info("Rest Template call " + endpoint);
            ResponseEntity<DiseaseTraitDto> entity = restTemplate.exchange(endpoint,
                    HttpMethod.PUT, httpEntity, DiseaseTraitDto.class);
        }
    }



        public List<TraitUploadReport> createDiseaseTrait(List<DiseaseTrait> diseaseTraits,User user) {
        List<TraitUploadReport> report = new ArrayList<>();
        diseaseTraits.forEach(diseaseTrait -> {
            try {
                Optional<DiseaseTrait> optDiseaseTrait = getDiseaseTraitByTraitName(diseaseTrait.getTrait());
                if(optDiseaseTrait.isPresent())
                    throw new CannotCreateTraitWithDuplicateNameException("Trait already exists with name"+optDiseaseTrait.get().getTrait());
                diseaseTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
                diseaseTraitRepository.insert(diseaseTrait);
                report.add(new TraitUploadReport(diseaseTrait.getTrait(),"Trait successfully Inserted : "+diseaseTrait.getTrait(),null));
            } catch(DataAccessException ex){
                report.add(new TraitUploadReport(diseaseTrait.getTrait(),"Trait Insertion failed as Trait already exists : "+diseaseTrait.getTrait(),null));
            } catch(CannotCreateTraitWithDuplicateNameException ex){
                report.add(new TraitUploadReport(diseaseTrait.getTrait(),"Trait Insertion failed as Trait already exists : "+diseaseTrait.getTrait(),null));
            }
        });
        //DiseaseTrait diseaseTraitInserted = diseaseTraitRepository.insert(diseaseTrait);
        return report;
    }

    public DiseaseTrait updateDiseaseTrait(DiseaseTrait diseaseTrait) {
        log.info("Inside updateDiseaseTrait()");
        DiseaseTrait diseaseTraitUpdated = diseaseTraitRepository.save(diseaseTrait);
        return diseaseTraitUpdated;
    }

    @Override
    public void deleteDiseaseTrait(List<String> diseaseTraitIds) {
        List<String> errorTraits = new ArrayList<>();
        List<String> errorStudyTraits = new ArrayList<>();

        diseaseTraitIds.forEach(traitId -> {
                if(!getDiseaseTrait(traitId).isPresent())
                    errorTraits.add(traitId);
                if(!checkForLinkedStudies(traitId)) {
                    String traitname = "";
                    Optional<DiseaseTrait> diseaseTraitOptional = getDiseaseTrait(traitId);
                    if(diseaseTraitOptional.isPresent())
                        traitname = getDiseaseTrait(traitId).get().getTrait();
                    diseaseTraitRepository.deleteById(traitId);
                    callOldCurationServiceDelete(traitname);
                }
                else
                    errorStudyTraits.add(traitId);
            });

        String errorTraitsMessage = errorStudyTraits.stream().collect(Collectors.joining(","));
        String errorStudyTraitsMessage = errorStudyTraits.stream().collect(Collectors.joining(","));
        if(!errorTraits.isEmpty())
            throw new EntityNotFoundException("Disease Trait not found:"+errorTraitsMessage);
        if(!errorStudyTraits.isEmpty())
            throw new CannotDeleteTraitException("Can't delete Trait as is linked to a study:"+errorStudyTraitsMessage);
    }

    public boolean checkForLinkedStudies(String traitId) {

     List<Study> studies = studyRepository.findByDiseaseTraitsContains(traitId);
     if( studies != null && !studies.isEmpty()) {
         return true;
     }
     return false;
    }

    public Optional<DiseaseTrait> getDiseaseTraitByTraitName(String traitName) {
        return diseaseTraitRepository.findByTraitIgnoreCase(traitName);
    }

    public Optional<DiseaseTrait> getDiseaseTrait(String traitId) {
        return diseaseTraitRepository.findById(traitId);
    }

    public Page<DiseaseTrait> getDiseaseTraits(String trait, String studyId, Pageable page) {
        if(trait !=null && studyId != null)
            return diseaseTraitRepository.findByStudyIdsContainsAndTrait(studyId, trait, page);
        else if(trait != null)
            return diseaseTraitRepository.findByTraitContainingIgnoreCase(trait, page);
        else if(studyId != null)
            return diseaseTraitRepository.findByStudyIdsContains(studyId, page);

        return diseaseTraitRepository.findAll(page);
    }

    public DiseaseTrait saveDiseaseTrait(String traitId, DiseaseTraitDto diseaseTraitDto, User user) {
        Optional<DiseaseTrait> optDiseaseTrait = this.getDiseaseTrait(traitId);
        if (optDiseaseTrait.isPresent()) {
            DiseaseTrait diseaseTrait = optDiseaseTrait.get();
            Optional.ofNullable(diseaseTraitDto.getTrait()).ifPresent(trait -> diseaseTrait.setTrait(diseaseTraitDto.getTrait()));
            List<String> studies = diseaseTrait.getStudyIds();
            Optional.ofNullable(diseaseTraitDto.getStudies()).ifPresent(studys -> studys.forEach(studyID -> {
                if (!studies.contains(studyID))
                    studies.add(studyID);
            }));
            diseaseTrait.setUpdated(new Provenance(DateTime.now(), user.getId()));
            return diseaseTraitRepository.save(diseaseTrait);
        } else {
            throw new EntityNotFoundException("Disease Trait Not found");
        }
    }


    @Cacheable(value = "diseaseTraitAnalysis", key = "#analysisId")
    public AnalysisCacheDto similaritySearch(List<AnalysisDTO> diseaseTraitAnalysisDTOS, String analysisId, double threshold) {
        LevenshteinDistance lv = new LevenshteinDistance();
        CosineDistance cd = new CosineDistance();

        List<DiseaseTrait> diseaseTraits = diseaseTraitRepository.findAll();
        List<AnalysisDTO> analysisReport = new ArrayList<>();
        diseaseTraitAnalysisDTOS
                .forEach(diseaseTraitAnalysisDTO ->
                        diseaseTraits.forEach(diseaseTrait -> {
                                    String trait = diseaseTrait.getTrait();
                                    String userTerm = diseaseTraitAnalysisDTO.getUserTerm();
                                    log.info("Trait ->"+trait);
                                    log.info("userTerm ->"+userTerm);

                                    double cosineDistance = cd.apply(userTerm, trait);
                                    double levenshteinDistance = ((double) lv.apply(userTerm, trait)) / Math.max(userTerm.length(), trait.length());
                                    double cosineSimilarityPercent = Math.round((1 - cosineDistance) * 100);
                                    double levenshteinSimilarityPercent = Math.round((1 - levenshteinDistance) * 100);
                                    double chosen = Math.max(cosineSimilarityPercent, levenshteinSimilarityPercent);
                                    log.info("cosineDistance : {}",cosineDistance);
                                    log.info("levenshteinDistance : {}",levenshteinDistance);
                                    log.info("cosineSimilarityPercent : {}",cosineSimilarityPercent);
                                    log.info("levenshteinSimilarityPercent : {}",levenshteinSimilarityPercent);
                                    log.info("chosen : {}",chosen);
                                    log.info("threshold : {}",threshold);

                            if (chosen >= threshold) {

                                        AnalysisDTO report = AnalysisDTO.builder()
                                                .userTerm(userTerm)
                                                .similarTerm(trait)
                                                .degree(chosen).build();
                                        analysisReport.add(report);
                                        log.info("Inside Analysis Report Blick :{}",analysisReport );
                                    }
                                }
                        ));

        AnalysisCacheDto analysisCacheDto = AnalysisCacheDto.builder()
                .uniqueId(analysisId)
                .analysisResult(analysisReport).build();

        return analysisCacheDto;

    }

}
