package uk.ac.ebi.spot.gwas.curation.rest.dto;

import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.dto.*;

import java.util.List;

public class SubmissionDtoAssembler {

    public static SubmissionDto assemble(Submission submission,
                                         PublicationDto publication,
                                         BodyOfWorkDto bodyOfWork,
                                         List<FileUploadDto> fileUploads,
                                         ProvenanceDto created,
                                         ProvenanceDto lastUpdated,
                                         ProvenanceDto editTemplate,
                                         LockDetailsDto lockDetailsDto) {
        return SubmissionDto
                .builder()
                .submissionId(submission.getId())
                .publication(publication)
                .bodyOfWork(bodyOfWork)
                .files(fileUploads)
                .globusFolder(submission.getGlobusFolderId())
                .globusOriginId(submission.getGlobusOriginId())
                .studyCount(submission.getStudies().size())
                .sampleCount(submission.getSamples().size())
                .associationCount(submission.getAssociations().size())
                .submissionStatus(submission.getOverallStatus())
                .metadataStatus(submission.getMetadataStatus())
                .summaryStatisticsStatus(submission.getSummaryStatsStatus())
                .dateSubmitted(submission.getDateSubmitted())
                .provenanceType(submission.getProvenanceType())
                .created(created)
                .lastUpdated(lastUpdated)
                .editTemplate(editTemplate)
                .lockDetails(lockDetailsDto)
                .agreedToCc0(submission.isAgreedToCc0())
                .openTargetsFlag(submission.getOpenTargetsFlag())
                .userRequestedFlag(submission.getUserRequestedFlag())
                .type(submission.getType())
                .build();
    }
}
