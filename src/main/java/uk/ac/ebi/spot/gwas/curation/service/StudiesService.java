package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitStudyMappingDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;

import java.util.List;

public interface StudiesService {


    Study getStudy(String studyId);

    Study getStudyByAccession(String accessionId);

    Study updateStudies(Study study);

    public Page<Study> getStudies(String submissionId,  Pageable page);

    public List<DiseaseTrait> getDiseaseTraitsByStudyId(String studyId);

    public List<String> getTraitsIDsFromDB(List<DiseaseTraitDto> diseaseTraitDtos, String studyId);

    List<TraitUploadReport> updateTraitsForStudies(List<StudyPatchRequest> studyPatchRequests);

    List<TraitUploadReport> updateEfoTraitsForStudies(List<EfoTraitStudyMappingDto> efoTraitStudyMappingDtos);

}
