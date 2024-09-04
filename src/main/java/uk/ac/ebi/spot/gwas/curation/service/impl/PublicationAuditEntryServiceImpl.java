package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.CurationStatusSnapshotStatsEntryRepository;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuditEntryRepository;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationWeeklyStatsEntryRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.CurationQueueStatsAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.CurationStatusSnapshotStatsAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationWeeklyStatsAssembler;
import uk.ac.ebi.spot.gwas.curation.service.CurationStatusService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuditEntryService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationQueueStats;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationStatusSnapshotStats;
import uk.ac.ebi.spot.gwas.deposition.audit.PublicationWeeklyStats;
import uk.ac.ebi.spot.gwas.deposition.audit.constants.PublicationEventType;
import uk.ac.ebi.spot.gwas.deposition.constants.SubmissionType;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuditEntry;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PublicationAuditEntryServiceImpl implements PublicationAuditEntryService {

    private static final Logger log = LoggerFactory.getLogger(PublicationAuditEntryServiceImpl.class);
    @Autowired
    PublicationAuditEntryRepository publicationAuditEntryRepository;

    @Autowired
    PublicationWeeklyStatsEntryRepository publicationWeeklyStatsEntryRepository;

    @Autowired
    PublicationWeeklyStatsAssembler publicationWeeklyStatsAssembler;

    @Autowired
    CurationStatusSnapshotStatsEntryRepository curationStatusSnapshotStatsEntryRepository;

    @Autowired
    CurationStatusSnapshotStatsAssembler curationStatusSnapshotStatsAssembler;

    @Autowired
    SubmissionService submissionService;

    @Autowired
    PublicationService publicationService;


    @Autowired
    CurationStatusService curationStatusService;

    @Autowired
    CurationQueueStatsAssembler curationQueueStatsAssembler;

    Map<String, String> curationStatusMap =  null;


    public PublicationAuditEntry getAuditEntry(String auditEntryId) {
      return   publicationAuditEntryRepository.findById(auditEntryId)
                .orElse(null);

    }

    public List<CurationQueueStats> getCurationQueueStats() {
        List<Publication> totalPubs = publicationService.getTotalPublications();
        log.info("Size of totalPubs {}", totalPubs.size());
        return totalPubs.stream().
                map(pub -> curationQueueStatsAssembler.assembleFromPublication(pub, CurationUtil.getCurrentDate()))
                .collect(Collectors.toList());
    }


   public  Page<PublicationAuditEntry> getPublicationAuditEntries(String pubId, Pageable pageable){
      return publicationAuditEntryRepository.findByPublicationId(pubId, pageable);
   }

    public CurationStatusSnapshotStats getCurationStatusSnapshotStats(DateTime dateTime) {
        List<PublicationAuditEntry> pubEntries = publicationAuditEntryRepository.findByTimestampAfter(dateTime);
        log.info("Size of pubEntries {}", pubEntries.size());
        List<Publication> totalPubs = publicationService.getTotalPublications();
        curationStatusMap   = curationStatusService.getCurationStatusMap();
        Integer awaitingSubmissionCount = getPublicationsWithCurationStatus( totalPubs, "Awaiting submission").size();
        Integer awaitingLiteratureCount = getPublicationsWithCurationStatus(totalPubs, "Awaiting literature").size();
        List<Publication> awaitingLevel2CountList = getPublicationsWithCurationStatus(totalPubs,"Submission complete");
        Integer awaitingLevel2Count = awaitingLevel2CountList.size();
        Integer studiesAwaitingLevel2Count = filterPublicationStudies(awaitingLevel2CountList);
        List<Publication>  awaitingLevel2WithAsscns =   filterPublicationWithAsscns(awaitingLevel2CountList);
        Integer awaitingLevel2WithAsscnsCount = awaitingLevel2WithAsscns.size();
        Integer studiesAwaitingLevel2WithAsscnsCount = filterPublicationStudiesWithAsscns(awaitingLevel2WithAsscns);
        List<Publication> awaitingEfoCountList = getPublicationsWithCurationStatus( totalPubs,"Awaiting EFO");
        Integer awaitingEfoCount = awaitingEfoCountList.size();
        Integer studiesAwaitingEfoCount = filterPublicationStudies(awaitingEfoCountList);
        List<Publication> outStandingQueryCountList = getPublicationsWithCurationStatus( totalPubs,"Pending curation query");
        Integer outStandingQueryCount = outStandingQueryCountList.size();
        Integer studiesOutStandingQueryCount = filterPublicationStudies(outStandingQueryCountList);
        Long publishedCount =  filterPublicationAuditEntries(pubEntries, "Publish Study", PublicationEventType.CURATION_STATUS_UPDATED.name());
        CurationStatusSnapshotStats curationStatusSnapshotStats = CurationStatusSnapshotStats.builder()
                .week(CurationUtil.getCurrentDate())
                .awaitSubmissionCount(String.format("%s %s",awaitingSubmissionCount.intValue(), "Pub"))
                .awaitLiteratureCount(String.format("%s %s",awaitingLiteratureCount.intValue(), "Pub"))
                .awaitingLevel2Completion(String.format("%s %s (%s %s)",awaitingLevel2Count.intValue(), "Pub", studiesAwaitingLevel2Count, "Stu" ))
                .awaitingLevel2CompletionWithAccsns(String.format("%s %s (%s %s)",awaitingLevel2WithAsscnsCount.intValue(), "Pub", studiesAwaitingLevel2WithAsscnsCount, "Stu"))
                .awaitingEFOMapping(String.format("%s %s (%s %s)",awaitingEfoCount.intValue(), "Pub", studiesAwaitingEfoCount, "Stu" ))
                .outstandingQuery(String.format("%s %s (%s %s)",outStandingQueryCount.intValue(), "Pub", studiesOutStandingQueryCount, "Stu" ))
                .totalPublished(String.format("%s %s",publishedCount.intValue(), "Pub"))
                .build();

        curationStatusSnapshotStatsEntryRepository.save(curationStatusSnapshotStatsAssembler.assemble(curationStatusSnapshotStats));
        return curationStatusSnapshotStats;

    }


    public PublicationWeeklyStats getPublicationStats(DateTime dateTime) {
        List<PublicationAuditEntry> pubEntries = publicationAuditEntryRepository.findByTimestampAfter(dateTime);
        log.info("Size of pubEntries {}", pubEntries.size());
        Long pmidSubCompCount = filterPublicationAuditEntries(pubEntries, "Submission complete",PublicationEventType.CURATION_STATUS_UPDATED.name());
        Long pmidAddedCount = pubEntries.stream().filter(entry -> entry.getEvent().equals(PublicationEventType.PMID_CREATED.name()))
                .count();
        Long pmidLevel2Count = filterPublicationAuditEntries(pubEntries,"Level 2 curation done",PublicationEventType.CURATION_STATUS_UPDATED.name());
        Integer pmidLevel2StudyCount = filterPublicationAuditEntriesStudies(pubEntries, "Level 2 curation done", PublicationEventType.CURATION_STATUS_UPDATED.name());

        Long pmidsPubStudyCount = pubEntries.stream().filter(entry -> entry.getEvent().equals
                        (PublicationEventType.CURATION_STATUS_UPDATED.name()))
                .filter(entry -> entry.getEventDetails().contains("Publish Study"))
                .count();
        Integer studiesPubStudyCount = filterPublicationAuditEntriesStudies(pubEntries, "Publish Study", PublicationEventType.CURATION_STATUS_UPDATED.name());
        Map<String, String> pubUserMapPubStudy= new HashMap<>();
        Map<String, String> pubUserMapSubComp = new HashMap<>();
        pubEntries.forEach(pubEntry -> {
            if (pubUserMapSubComp.get(pubEntry.getPublicationId()) == null ) {
                pubUserMapSubComp.put(pubEntry.getPublicationId(), getPublicationAuditPairForSubComplete(pubEntry));
            }
        });
        pubEntries.forEach(pubEntry ->  {
            if (pubUserMapPubStudy.get(pubEntry.getPublicationId()) == null ) {
                pubUserMapPubStudy.put(pubEntry.getPublicationId(), getPublicationAuditPairForPubStudies(pubEntry));
            }
        });
        AtomicInteger countSingleLevelComplete = new AtomicInteger();
        Set<String> uniquePubs = new HashSet<>();
        pubEntries.forEach(pubEntry -> {
            log.info("Publication is {}",pubEntry.getPublicationId());
            log.debug("Pub Event Details is {}",pubEntry.getEventDetails());
            String userEmailSubComp = pubUserMapPubStudy.get(pubEntry.getPublicationId());
            log.debug("userEmailSubComp {}",userEmailSubComp);
            String userEmailPubStudy = pubUserMapSubComp.get(pubEntry.getPublicationId());
            log.debug("userEmailPubStudy {}",userEmailPubStudy);
            if(!uniquePubs.contains(pubEntry.getPublicationId())) {
                if (userEmailSubComp != null && userEmailPubStudy != null) {
                    if (userEmailSubComp.equalsIgnoreCase(userEmailPubStudy)) {
                        countSingleLevelComplete.getAndIncrement();
                    }
                }
            }
            uniquePubs.add(pubEntry.getPublicationId());
        });

        PublicationWeeklyStats publicationWeeklyStats = PublicationWeeklyStats.builder()
                .week(CurationUtil.getCurrentDate())
                .pmidsAdded(String.format("%s %s",pmidAddedCount.intValue(), "Pub"))
                .pmidsLevel2Done(String.format("%s %s (%s %s)",pmidLevel2Count.intValue(), "Pub", pmidLevel2StudyCount, "Stu" ))
                .pmidsSubComplete(String.format("%s %s",pmidSubCompCount.intValue(), "Pub"))
                .pmidsPublished(String.format("%s %s (%s %s)",pmidsPubStudyCount.intValue(), "Pub", studiesPubStudyCount, "Stu" ))
                .pmidsSingleLevelComplete(String.format("%s %s",countSingleLevelComplete.intValue(), "Pub"))
                .build();
        log.info("Created the publish weekly stats");
        publicationWeeklyStatsEntryRepository.save(publicationWeeklyStatsAssembler.assemble(publicationWeeklyStats));
        return publicationWeeklyStats;
    }

    private  String getPublicationAuditPairForPubStudies(PublicationAuditEntry publicationAuditEntry) {
       return  Optional.ofNullable(publicationAuditEntry)
                .filter(pubAudit -> pubAudit.getEvent().equals(PublicationEventType.CURATION_STATUS_UPDATED.name()))
                .filter(pubAudit -> pubAudit.getEventDetails().contains("Publish Study"))
                .map(pubAudit -> pubAudit.getUserId())
                .orElse(null);

    }

    private  String getPublicationAuditPairForSubComplete(PublicationAuditEntry publicationAuditEntry) {
        return  Optional.ofNullable(publicationAuditEntry)
                .filter(pubAudit -> pubAudit.getEvent().equals(PublicationEventType.CURATION_STATUS_UPDATED.name()))
                .filter(pubAudit -> pubAudit.getEventDetails().contains("Submission complete"))
                .map(pubAudit -> pubAudit.getUserId())
                .orElse(null);
    }

    private Long filterPublicationAuditEntries(List<PublicationAuditEntry> pubEntries, String qualifier, String event ) {
       return  pubEntries.stream().filter(entry -> entry.getEvent().equals(event))
                .filter(entry -> entry.getEventDetails().contains(qualifier))
                .count();
    }


    private Integer filterPublicationAuditEntriesStudies(List<PublicationAuditEntry> pubEntries, String qualifier, String event ) {
        return  pubEntries.stream().filter(entry -> entry.getEvent().equals(event))
                .filter(entry -> entry.getEventDetails().contains(qualifier))
                .map(PublicationAuditEntry::getPublicationId)
                .map(pubId -> submissionService.getSubmissionForPublication(pubId))
                .filter(subList -> subList != null && !subList.isEmpty())
                .map(subList -> getMetaDataSubmission(subList))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(sub -> sub.getStudies().size() )
                .reduce(0, (a,b)  -> a + b);
    }

    private Integer filterPublicationStudies(List<Publication> pubEntries ) {
        return  pubEntries.stream()
                .map(pub -> submissionService.getSubmissionForPublication(pub.getId()))
                .filter(subList -> subList != null && !subList.isEmpty())
                .map(subList -> getMetaDataSubmission(subList))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(sub -> sub.getStudies().size() )
                .reduce(0, (a,b)  -> a + b);
    }



    private List<Publication>  filterPublicationWithAsscns(List<Publication> pubEntries) {
        return  pubEntries.stream()
                .filter(pub -> filterSubmissionWithAssociations(submissionService.getSubmissionForPublication(pub.getId())))
                .collect(Collectors.toList());

    }

    public Boolean filterSubmissionWithAssociations(List<Submission> submissions) {
       return Optional.ofNullable(submissions).filter(subList -> subList != null && !subList.isEmpty())
                .map(subList -> getMetaDataSubmission(subList))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(sub -> sub.getAssociations().size() > 0)
                .map(sub -> true)
                .orElse(false);
    }
    private Integer filterPublicationStudiesWithAsscns(List<Publication> pubEntries) {
        return  pubEntries.stream()
                .map(pub -> submissionService.getSubmissionForPublication(pub.getId()))
                .filter(subList -> subList != null && !subList.isEmpty())
                .map(subList -> getMetaDataSubmission(subList))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(sub -> sub.getAssociations().size() > 0)
                .map(sub -> sub.getStudies().size() )
                .reduce(0, (a,b)  -> a + b);
    }

    private List<Publication> getPublicationsWithCurationStatus(List<Publication> totalPubs, String curationStatus){
        return totalPubs.stream()
                .filter(publication -> publication.getCurationStatusId() != null)
                .filter(publication -> getCurationStatus(publication.getCurationStatusId())
                        .equals(curationStatus))
                .collect(Collectors.toList());
    }


    private String getCurationStatus(String curationStatusId) {
        return curationStatusMap.get(curationStatusId);
    }

    private Optional<Submission> getMetaDataSubmission(List<Submission> submissions) {
        return submissions.stream()
                .filter(sub -> sub.getType().equals(SubmissionType.METADATA.name()))
                .findFirst();
    }
}


