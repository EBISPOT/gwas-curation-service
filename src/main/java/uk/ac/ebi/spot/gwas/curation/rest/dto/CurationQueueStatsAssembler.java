package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.service.CurationStatusService;
import uk.ac.ebi.spot.gwas.curation.service.CuratorService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuthorService;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationQueueStats;
import uk.ac.ebi.spot.gwas.deposition.constants.SubmissionType;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CurationQueueStatsAssembler {

    private static final Logger log = LoggerFactory.getLogger(CurationQueueStatsAssembler.class);


    CurationStatusService curationStatusService;


    CuratorService curatorService;

    PublicationAuthorService publicationAuthorService;

    SubmissionService submissionService;

    Map<String, String> curationStatusMap =  null;

    Map<String, String> curatorMap =  null;

    @Autowired
    public CurationQueueStatsAssembler(SubmissionService submissionService,
                                       CurationStatusService curationStatusService,
                                       PublicationAuthorService publicationAuthorService,
                                       CuratorService curatorService) {
        this.submissionService = submissionService;
        this.curationStatusService = curationStatusService;
        this.publicationAuthorService = publicationAuthorService;
        this.curatorService = curatorService;
        curationStatusMap = curationStatusService.getCurationStatusMap();
        curatorMap = curatorService.getCuratorsMap();
    }

    public CurationQueueStats assembleFromPublication(Publication publication, String week) {
        return  CurationQueueStats.builder()
                .week(week)
                .pmid(publication.getPmid())
                .curationStatus(publication.getCurationStatusId() != null ? curationStatusMap.get(publication.getCurationStatusId()) : null)
                .curator(publication.getCuratorId() != null ? curatorMap.get(publication.getCuratorId()) : null)
                .firstAuthor(publication.getFirstAuthorId() != null ? getFirstAuthorFullName(publicationAuthorService.
                        getAuthorDetail(publication.getFirstAuthorId())) : publication.getFirstAuthor())
                .publicationDate(CurationUtil.getFormattedDate(publication.getPublicationDate()))
                .publicationYear(String.valueOf(publication.getPublicationDate().getYear()))
                .publicationMonth(String.valueOf(publication.getPublicationDate().getMonthOfYear()))
                .journal(publication.getJournal())
                .title(publication.getTitle())
                .associationCount(getAssociationsCount(publication.getId()))
                .studyCount(getStudiesCount(publication.getId()))
                .isOpenTargets(publication.getIsOpenTargets())
                .userRequested(publication.getIsUserRequested())
                .fullPvalueSet(hasFullPValue(publication.getId()))
                .build();
    }


    private String getFirstAuthorFullName(Optional<PublicationAuthor> optionalPublicationAuthor) {
        return optionalPublicationAuthor.map(publicationAuthor -> publicationAuthor.getFullName())
                .orElse("");
    }

    private List<Submission> getMatchingSubmissions(String pubId) {
        return submissionService.getSubmissionForPublication(pubId);
    }
    private Optional<Submission> getMatchingSubmission(String pubId) {
        List<Submission> submissionList = getMatchingSubmissions(pubId);
        return submissionList != null && !submissionList.isEmpty() ?  submissionList.stream()
                .filter(submission -> getMetaDataSubmission(submission) )
                .findFirst() : Optional.empty();
    }

    private Integer getAssociationsCount(String pubId) {
        Optional<Submission> optionalSubmission = getMatchingSubmission(pubId);
        return  optionalSubmission.isPresent() ? optionalSubmission.map(submission ->
                submission.getAssociations().size()).orElse(0) : 0;
    }

    private Integer getStudiesCount(String pubId) {
        Optional<Submission> optionalSubmission = getMatchingSubmission(pubId);
        return  optionalSubmission.isPresent() ? optionalSubmission.map(submission ->
                submission.getStudies().size()).orElse(0) : 0;
    }
    private Boolean getMetaDataSubmission(Submission submission) {
        return submission.getType().equals(SubmissionType.METADATA.name());
    }

    private Boolean hasFullPValue(String pubId) {
        List<Submission> submissionList = getMatchingSubmissions(pubId);
        return submissionList != null &&  !submissionList.isEmpty() ? submissionList.stream()
                .anyMatch(sub -> submissionService.findSumstatsEntries(sub.getId())) : false;

    }

}
