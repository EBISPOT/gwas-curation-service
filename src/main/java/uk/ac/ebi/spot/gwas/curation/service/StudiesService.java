package uk.ac.ebi.spot.gwas.curation.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;

import java.util.List;
import java.util.stream.Stream;

public interface StudiesService {


    ResponseEntity<String> updateFileType(FileTypeUpdateRequestDto request);

    Study getStudy(String studyId);

    Stream<Study> getStudies(List<String> ids);

    Study getStudyByAccession(String accessionId, String submissionId);

    Study updateStudies(Study study);

    String diffDiseaseTrait(String submissionId, String studyTag, String oldDiseaseTraitId, String newDiseaseTraitId);

    String diffEFOTrait(String submissionId, String studyTag, List<String> oldEFOTraitIds, List<String> newEFOTraitIds);

    public Page<Study> getStudies(String submissionId,  Pageable page);

    public Page<StudySolr> getStudies(Pageable page, SearchStudyDTO searchStudyDTO);

    public DiseaseTrait getDiseaseTraitsByStudyId(String studyId);


    UploadReportWrapper updateMultiTraitsForStudies(List<MultiTraitStudyMappingDto> multiTraitStudyMappingDtos, String submissionId);

    List<StudySampleDescPatchRequest> updateSampleDescription(List<StudySampleDescPatchRequest> studySampleDescPatchRequests, String submissionId);

    public byte[] uploadSampleDescriptions(List<StudySampleDescPatchRequest> studySampleDescPatchRequests, String submissionId);


    void sendMetaDataMessageToQueue(String submissionId);

}
