package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;

import java.util.List;

public interface StudiesService {


    Study getStudy(String studyId);

    Study getStudyByAccession(String accessionId, String submissionId);

    Study updateStudies(Study study);

    public Page<Study> getStudies(String submissionId,  Pageable page);

    public DiseaseTrait getDiseaseTraitsByStudyId(String studyId);


    List<TraitUploadReport> updateTraitsForStudies(List<StudyPatchRequest> studyPatchRequests, String submissionId);

    List<TraitUploadReport> updateEfoTraitsForStudies(List<EfoTraitStudyMappingDto> efoTraitStudyMappingDtos, String submissionId);

    List<StudySampleDescPatchRequest> updateSampleDescription(List<StudySampleDescPatchRequest> studySampleDescPatchRequests, String submissionId);

}
