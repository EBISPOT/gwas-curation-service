package uk.ac.ebi.spot.gwas.curation.service.impl;

import com.mongodb.client.*;
import static com.mongodb.client.model.Filters.*;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.oracle.repository.DiseaseTraitsRepository;
import uk.ac.ebi.spot.gwas.curation.oracle.repository.EFOTraitRepository;
import uk.ac.ebi.spot.gwas.curation.rabbitmq.SubmissionImportMQProducer;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationRepository;
import uk.ac.ebi.spot.gwas.curation.repository.SubmissionRepository;
import uk.ac.ebi.spot.gwas.curation.repository.SummaryStatsEntryRepository;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.deposition.domain.*;
import uk.ac.ebi.spot.gwas.deposition.dto.SubmissionDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.dto.ingest.BodyOfWorkDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.deposition.exception.StudiesWithoutTraitsException;
import uk.ac.ebi.spot.gwas.deposition.exception.TraitsNotSyncedException;
import uk.ac.ebi.spot.gwas.deposition.util.DepositionUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private CuratorAuthService curatorAuthService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private SummaryStatsEntryRepository summaryStatsEntryRepository;
    @Autowired
    @Qualifier("restTemplateCuration")
    RestTemplate restTemplate;

    @Autowired
    RestInteractionConfig restInteractionConfig;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    StudiesService studiesService;

    @Autowired
    EFOTraitRepository  efoTraitRepository;

    @Autowired
    EfoTraitService efoTraitService;

    @Autowired
    DiseaseTraitsRepository diseaseTraitRepository;

    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    SubmissionImportMQProducer submissionImportMQProducer;

    @Autowired
    CurationStatusService curationStatusService;




    @Override
    public Submission getSubmission(String submissionId) {
        Optional<Submission> optionalSubmission = submissionRepository.findByIdAndArchived(submissionId, false);

        if (!optionalSubmission.isPresent()) {
            log.error("Unable to find submission: {}", submissionId);
            throw new EntityNotFoundException("Unable to find submission: " + submissionId);
        }
        log.info("Submission successfully retrieved: {}", optionalSubmission.get().getId());
        return optionalSubmission.get();
    }

    @Override
    public Page<Submission> getSubmissions(String pubId, SearchSubmissionDTO searchSubmissionDTO, Pageable page) {


             if(searchSubmissionDTO != null){
                String bowId = searchSubmissionDTO.getBowId();
                 List<String> metaStatus = null;
                 List<String> ssStatus = null;
                 List<String> overAllStatus = null;
                 List<String> lockStatus = null;
                 if(searchSubmissionDTO.getMetaStatus() != null){
                    metaStatus = Arrays.asList(searchSubmissionDTO.getMetaStatus().split("\\|"));
                    }

                 if(searchSubmissionDTO.getSsStatus() != null){
                     ssStatus = Arrays.asList(searchSubmissionDTO.getSsStatus().split("\\|"));
                 }

                 if(searchSubmissionDTO.getSubmissionStatus() != null){
                     overAllStatus = Arrays.asList(searchSubmissionDTO.getSubmissionStatus().split("\\|"));
                 }

                 if(searchSubmissionDTO.getLockStatus() != null){
                     lockStatus = Arrays.asList(searchSubmissionDTO.getLockStatus().split("\\|"));
                 }

                if(pubId != null)
                    return submissionRepository.findByPublicationIdAndArchived(pubId, false, page);
                else if(bowId != null)
                    return submissionRepository.findByBodyOfWorksContainsAndArchived(bowId, false, page);
                else if(metaStatus != null && ssStatus != null && overAllStatus != null && lockStatus != null )
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, ssStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && ssStatus != null && overAllStatus != null)
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndOverallStatusInAndArchived(
                            metaStatus, ssStatus, overAllStatus, false, page);
                else if(metaStatus != null && ssStatus != null && lockStatus != null)
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, ssStatus, lockStatus, false, page);
                else if(ssStatus != null && overAllStatus != null && lockStatus != null)
                    return  submissionRepository.findBySummaryStatsStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(
                            ssStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && overAllStatus != null && lockStatus != null)
                    return  submissionRepository.findByMetadataStatusInAndOverallStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, overAllStatus, lockStatus, false, page);
                else if(metaStatus != null && ssStatus != null)
                    return submissionRepository.findByMetadataStatusInAndSummaryStatsStatusInAndArchived(
                            metaStatus, ssStatus, false, page);
                else if(metaStatus != null && overAllStatus != null)
                    return submissionRepository.findByMetadataStatusInAndOverallStatusInAndArchived(
                            metaStatus, overAllStatus, false, page);
                else if(ssStatus != null && overAllStatus != null)
                    return submissionRepository.findBySummaryStatsStatusInAndOverallStatusInAndArchived(
                            ssStatus, overAllStatus, false, page);
                else if(metaStatus != null && lockStatus != null)
                    return submissionRepository.findByMetadataStatusInAndLockDetails_StatusInAndArchived(
                            metaStatus, lockStatus, false, page);
                else if(ssStatus != null && lockStatus != null)
                    return submissionRepository.findBySummaryStatsStatusInAndLockDetails_StatusInAndArchived(
                            ssStatus, lockStatus, false, page);
                else if(metaStatus != null)
                    return submissionRepository.findByMetadataStatusInAndArchived(metaStatus , false, page);
                else if(ssStatus != null)
                    return submissionRepository.findBySummaryStatsStatusInAndArchived(ssStatus , false, page);
                else if(overAllStatus != null)
                    return submissionRepository.findByOverallStatusInAndArchived(overAllStatus , false, page);
                else if(lockStatus != null)
                    return submissionRepository.findByLockDetails_StatusInAndArchived(lockStatus , false, page);
            }

             return submissionRepository.findByArchived(false, page);
    }


    @Override
    public Submission getSubmission(String submissionId, User user) {
        log.info("Retrieving submission: {}", submissionId);
        Optional<Submission> optionalSubmission = curatorAuthService.isCurator(user) ?
                submissionRepository.findByIdAndArchived(submissionId, false) :
                submissionRepository.findByIdAndArchivedAndCreated_UserId(submissionId, false, user.getId());
        if (!optionalSubmission.isPresent()) {
            log.error("Unable to find submission: {}", submissionId);
            throw new EntityNotFoundException("Unable to find submission: " + submissionId);
        }
        log.info("Submission successfully retrieved: {}", optionalSubmission.get().getId());
        return optionalSubmission.get();
    }


    @Override
    public Submission   patchSubmission(SubmissionDto submissionDto, String submissionId) {
        Optional<Submission> submissionOptional = submissionRepository.findById(submissionId);
        if (!submissionOptional.isPresent()) {
            log.error("Unable to find submission: {}", submissionId);
            throw new EntityNotFoundException("Unable to find submission: " + submissionId);
        }
        Submission submission = submissionOptional.get();
        Publication publication = null;
        if(submission.getPublicationId() != null) {
            publication = publicationService.getPublicationDetailsByPmidOrPubId(submission.getPublicationId(), false);
        }
        log.info(" Submission status for {} is {}", submissionId, submissionDto.getSubmissionStatus());
        if (submissionDto.getSubmissionStatus() != null) {
            if (uk.ac.ebi.spot.gwas.deposition.constants.SubmissionType.SUMMARY_STATS.name().equals(submission.getType())) {
                submission.setOverallStatus(submissionDto.getSubmissionStatus());
            }
            if(!isAllStudiesHaveTraits(submissionId)) {
                throw new StudiesWithoutTraitsException("Some studies exist for submission without traits, Please assign traits to all studies");
            }
            List<String> unsyncedEfos = isEfoTraitsSyncedToOracle(submissionId);
            if(unsyncedEfos != null && !unsyncedEfos.isEmpty()) {
                throw new TraitsNotSyncedException("Some EFO traits have not synced to Oracle -: " + String.join(",", unsyncedEfos));
            }
            List<String> unsyncedTraits = isDiseaseTraitSyncedToOracle(submissionId);
            if(unsyncedTraits != null && !unsyncedTraits.isEmpty()) {
                throw new TraitsNotSyncedException("Some disease traits have not synced to Oracle -: " + String.join(",", unsyncedTraits));
            }
            if(publication != null) {
                if(submission.getOverallStatus() != null && submission.getOverallStatus().equals("CURATION_COMPLETE")) {
                    if(publication.getStatus() != null && (publication.getStatus().equals("PUBLISHED") || publication.getStatus().equals("PUBLISHED_WITH_SS"))) {
                        publication.setStatus("UNDER_SUBMISSION");
                        publicationRepository.save(publication);
                    }
                }
                publication.setCurationStatusId(curationStatusService.findCurationStatusByStatus("Import in Progress").getId());
                publicationRepository.save(publication);
            }
            SubmissionRabbitMessage submissionRabbitMessage = SubmissionRabbitMessage.builder()
                    .submissionId(submissionId)
                    .submissionType(submission.getType())
                    .build();
            submissionImportMQProducer.send(submissionRabbitMessage);
            submission.setOverallStatus(submissionDto.getSubmissionStatus());
        }
        return submissionRepository.save(submission);
    }

    private boolean isAllStudiesHaveTraits(String submissionId) {
        MongoCollection<Document> studiesCollection = mongoTemplate.getCollection("studies");
        Bson query = and(
                eq("submissionId", submissionId),
                or(
                        eq("efoTraits", null),
                        size("efoTraits", 0),
                        eq("diseaseTrait", null),
                        eq("diseaseTrait", "")
                )
        );
        return studiesCollection.countDocuments(query) == 0;
    }

    private List<String> isEfoTraitsSyncedToOracle(String submissionId) {
      Set<String> efoShortForms =  studiesService.getEfoTraitsForSubmission(submissionId);
      return efoShortForms.stream()
                .filter(shortform -> !efoTraitRepository.findByShortForm(shortform).isPresent())
                .map(shortForm -> efoTraitService.getEFOtraitByShortForm(shortForm))
                .map(EfoTrait::getShortForm)
                .collect(Collectors.toList());

    }

    private List<String> isDiseaseTraitSyncedToOracle(String submissionId) {
        Set<String> diseaseTraits = studiesService.getDiseaseTraitsForSubmission(submissionId);
        return diseaseTraits.stream()
                .filter(trait -> !diseaseTraitRepository.findByTrait(trait).isPresent())
                .map(trait -> diseaseTraitService.getDiseaseTraitByTraitName(trait))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(DiseaseTrait::getTrait)
                .collect(Collectors.toList());
    }


    public Map<String, SubmissionEnvelope> getSubmissions() {
        Map<String, SubmissionEnvelope> submissionList = new TreeMap<>();
        try {
            int i = 0;
            Map<String, Integer> params = new HashMap<>();
            params.put("page", i);
            log.info("Ingest uri is {}", restInteractionConfig.getIngestServiceUri()+restInteractionConfig.getSubmissionEnvelopeEndpoint());
            DepositionSubmission[] submissions =
                    restTemplate.getForObject(restInteractionConfig.getIngestServiceUri()+restInteractionConfig.getSubmissionEnvelopeEndpoint(), DepositionSubmission[].class, params);
            Arrays.stream(submissions).forEach(s -> {
                SubmissionEnvelope testSub = buildSubmission(s);
                submissionList.put(testSub.getId(), testSub);
            });
        } catch (Exception e) {
           log.error("Exception in getSubmissions() call"+e.getMessage(),e);
        }
        return submissionList;

    }


    public SubmissionEnvelope buildSubmission(DepositionSubmission depositionSubmission) {
        SubmissionEnvelope testSub = new SubmissionEnvelope();
        testSub.setId(depositionSubmission.getSubmissionId());
        testSub.setCurator(depositionSubmission.getCreated().getUser().getName());
        testSub.setStatus(depositionSubmission.getStatus());
        testSub.setCreated(depositionSubmission.getCreated().getTimestamp().toString(DateTimeFormat.forPattern("yyyy-MM-dd")));
        testSub.setImportStatus(ImportStatus.NOT_READY);
        testSub.setSubmissionType(DepositionUtil.getSubmissionType(depositionSubmission));
        if (depositionSubmission.getBodyOfWork() != null) {
            BodyOfWorkDto bodyOfWork = depositionSubmission.getBodyOfWork();
            if (bodyOfWork.getPmids() != null && bodyOfWork.getPmids().size() != 0) {
                testSub.setPubMedID(String.join(",", bodyOfWork.getPmids()));
            }
            if (bodyOfWork.getFirstAuthor() != null) {
                if (bodyOfWork.getFirstAuthor().getGroup() != null) {
                    testSub.setAuthor(bodyOfWork.getFirstAuthor().getGroup());
                } else {
                    testSub.setAuthor(bodyOfWork.getFirstAuthor().getFirstName() + ' ' +
                            bodyOfWork.getFirstAuthor().getLastName());
                }
            }
            testSub.setTitle(bodyOfWork.getTitle());
            testSub.setPublicationStatus(bodyOfWork.getStatus());
            testSub.setDoi(bodyOfWork.getPreprintServerDOI());
            if (testSub.getSubmissionType().equals(SubmissionType.UNKNOWN)) {
                testSub.setStatus("REVIEW");
            }
        } else if (depositionSubmission.getPublication() != null) {
            DepositionPublication publication = depositionSubmission.getPublication();
            testSub.setPubMedID(publication.getPmid());
            testSub.setAuthor(publication.getFirstAuthor());
            testSub.setTitle(publication.getTitle());
            testSub.setPublicationStatus(publication.getStatus());
            testSub.setDoi(publication.getDoi());
            if (testSub.getSubmissionType().equals(SubmissionType.UNKNOWN)) {
                testSub.setStatus("REVIEW");
            }
        }

        if (testSub.getStatus().equalsIgnoreCase("SUBMITTED") &&
                !testSub.getSubmissionType().equals(SubmissionType.PRE_PUBLISHED) &&
                !testSub.getSubmissionType().equals(SubmissionType.UNKNOWN)) {
            testSub.setImportStatus(ImportStatus.READY);
        }
        return testSub;
    }

    public List<Submission>  getSubmissionForPublication(String pubId) {
        return submissionRepository.findByPublicationId(pubId);
    }


   public Boolean findSumstatsEntries(String submissionId) {
    List<String> fileUploads =  submissionRepository.findById(submissionId)
                .map(sub -> sub.getFileUploads())
                .filter(uploads -> uploads != null & !uploads.isEmpty())
                .orElse(null);
        return fileUploads != null &&  !fileUploads.isEmpty() ? fileUploads.stream()
                .anyMatch(this::existSumstatsEntries) : false;
    }

    private Boolean existSumstatsEntries(String fileUploadId) {
        return !summaryStatsEntryRepository.findByFileUploadId(fileUploadId).isEmpty();
    }

}
