package uk.ac.ebi.spot.gwas.curation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rabbitmq.DiseaseTraitMQProducer;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitRabbitMessageAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotCreateTraitWithDuplicateNameException;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotDeleteTraitException;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    FileHandler fileHandler;

    @Autowired
    DiseaseTraitMQProducer diseaseTraitMQProducer;

    @Autowired
    DiseaseTraitRabbitMessageAssembler diseaseTraitRabbitMessageAssembler;

    public DiseaseTraitServiceImpl(DiseaseTraitRepository diseaseTraitRepository) {
        this.diseaseTraitRepository = diseaseTraitRepository;
    }

    // Sanitize helper: Normalises string and strips non-ASCII, i.e. é -> e
    private static String sanitizeTrait(String value) {
        value = Normalizer.normalize(value, Normalizer.Form.NFD);
        if (value == null) {
            return null;
        }
        String asciiOnly = value.replaceAll("[^\\x00-\\x7F]", "");
        return asciiOnly.replaceAll("\\s+", " ").trim();
    }

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait) {
        diseaseTrait.setTrait(sanitizeTrait(diseaseTrait.getTrait()));
        Optional<DiseaseTrait> optDiseaseTrait = getDiseaseTraitByTraitName(diseaseTrait.getTrait());
        if(!optDiseaseTrait.isPresent()) {
            DiseaseTrait insertedDiseaseTrait = diseaseTraitRepository.insert(diseaseTrait);
            sendMessage(insertedDiseaseTrait, "insert");
            return insertedDiseaseTrait;
        }
        else
        throw new CannotCreateTraitWithDuplicateNameException("Trait already exists with name"+optDiseaseTrait.get().getTrait());
    }


        public UploadReportWrapper createDiseaseTrait(List<DiseaseTrait> diseaseTraits, User user) {
        List<TraitUploadReport> report = new ArrayList<>();
        UploadReportWrapper uploadReportWrapper = new UploadReportWrapper();
        diseaseTraits.forEach(diseaseTrait -> {
            try {
                diseaseTrait.setTrait(sanitizeTrait(diseaseTrait.getTrait()));
                Optional<DiseaseTrait> optDiseaseTrait = getDiseaseTraitByTraitName(diseaseTrait.getTrait());
                if(optDiseaseTrait.isPresent())
                    throw new CannotCreateTraitWithDuplicateNameException("Trait already exists with name"+optDiseaseTrait.get().getTrait());
                diseaseTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
                DiseaseTrait insertedDiseaseTrait =  diseaseTraitRepository.insert(diseaseTrait);
                sendMessage(insertedDiseaseTrait, "insert");
                report.add(new TraitUploadReport(diseaseTrait.getTrait(),"Trait successfully Inserted : "+diseaseTrait.getTrait(),null));
            } catch(DataAccessException | CannotCreateTraitWithDuplicateNameException ex) {
                uploadReportWrapper.setHasErrors(true);
                report.add(new TraitUploadReport(diseaseTrait.getTrait(),"Trait Insertion failed as Trait already exists : "+diseaseTrait.getTrait(),null));
            }
        });
        //DiseaseTrait diseaseTraitInserted = diseaseTraitRepository.insert(diseaseTrait);
        uploadReportWrapper.setUploadReport(fileHandler.serializePojoToTsv(report));
        return uploadReportWrapper;
    }

    public DiseaseTrait updateDiseaseTrait(DiseaseTrait diseaseTrait) {
        diseaseTrait.setTrait(sanitizeTrait(diseaseTrait.getTrait()));
        log.info("Inside updateDiseaseTrait()");
        DiseaseTrait diseaseTraitUpdated = diseaseTraitRepository.save(diseaseTrait);
        sendMessage(diseaseTraitUpdated, "update");
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
                    if(diseaseTraitOptional.isPresent()) {
                        traitname = getDiseaseTrait(traitId).get().getTrait();
                        sendMessage(diseaseTraitOptional.get(), "delete");
                    }
                    diseaseTraitRepository.deleteById(traitId);
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

     List<Study> studies = studyRepository.findByDiseaseTrait(traitId);
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

         if(trait != null)
            return diseaseTraitRepository.findByTraitContainingIgnoreCase(trait, page);

        return diseaseTraitRepository.findAll(page);
    }

    public DiseaseTrait saveDiseaseTrait(String traitId, DiseaseTraitDto diseaseTraitDto, User user) {
        Optional<DiseaseTrait> optDiseaseTrait = this.getDiseaseTrait(traitId);
        if (optDiseaseTrait.isPresent()) {
            DiseaseTrait diseaseTrait = optDiseaseTrait.get();
            Optional.ofNullable(diseaseTraitDto.getTrait()).ifPresent(trait -> diseaseTrait.setTrait(sanitizeTrait(diseaseTraitDto.getTrait())));

            diseaseTrait.setUpdated(new Provenance(DateTime.now(), user.getId()));
            DiseaseTrait updatedDiseaseTrait = diseaseTraitRepository.save(diseaseTrait);
            sendMessage(updatedDiseaseTrait, "update");
            return updatedDiseaseTrait;
        } else {
            throw new EntityNotFoundException("Disease Trait Not found");
        }
    }


    //@Async
    @Cacheable(value = "diseaseTraitAnalysis", key = "#analysisId")
    public AnalysisCacheDto similaritySearch(List<AnalysisRequestDTO> diseaseTraitAnalysisDTOS, String analysisId, double threshold) {
        LevenshteinDistance lv = new LevenshteinDistance();
        CosineDistance cd = new CosineDistance();

        List<DiseaseTrait> diseaseTraits = diseaseTraitRepository.findAll();
        List<AnalysisDTO> analysisReport = new ArrayList<>();
        diseaseTraitAnalysisDTOS
                .forEach(diseaseTraitAnalysisDTO ->
                        diseaseTraits.stream().parallel().forEach(diseaseTrait -> {
                                    String trait = diseaseTrait.getTrait();
                                    String userTerm = diseaseTraitAnalysisDTO.getUserTerm();
                                    //log.info("Trait ->"+trait);
                                   // log.info("userTerm ->"+userTerm);

                                    double cosineDistance = cd.apply(userTerm, trait);
                                    double levenshteinDistance = ((double) lv.apply(userTerm, trait)) / Math.max(userTerm.length(), trait.length());
                                    double cosineSimilarityPercent = Math.round((1 - cosineDistance) * 100);
                                    double levenshteinSimilarityPercent = Math.round((1 - levenshteinDistance) * 100);
                                    double chosen = Math.max(cosineSimilarityPercent, levenshteinSimilarityPercent);
                                    /*log.info("cosineDistance : {}",cosineDistance);
                                    log.info("levenshteinDistance : {}",levenshteinDistance);
                                    log.info("cosineSimilarityPercent : {}",cosineSimilarityPercent);
                                    log.info("levenshteinSimilarityPercent : {}",levenshteinSimilarityPercent);
                                    log.info("chosen : {}",chosen);
                                    log.info("threshold : {}",threshold);*/

                            if (chosen >= threshold) {

                                        AnalysisDTO report = AnalysisDTO.builder()
                                                .userTerm(userTerm)
                                                .similarTerm(trait)
                                                .degree(chosen).build();
                                        synchronized(this) {
                                            analysisReport.add(report);
                                        }
                                       // log.info("Inside Analysis Report Blick :{}",analysisReport );
                                    }
                                }
                        ));

        AnalysisCacheDto analysisCacheDto = AnalysisCacheDto.builder()
                .uniqueId(analysisId)
                //.status(DepositionCurationConstants.ANALYSIS_STATUS_DONE)
                .analysisResult(analysisReport).build();

        return analysisCacheDto;

    }


    private void sendMessage(DiseaseTrait diseaseTrait, String operation) {
        diseaseTraitMQProducer.send(diseaseTraitRabbitMessageAssembler.assemble(diseaseTrait, operation));
    }
}
