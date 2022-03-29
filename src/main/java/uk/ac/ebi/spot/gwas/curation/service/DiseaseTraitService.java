package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.AnalysisCacheDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.AnalysisDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public interface DiseaseTraitService {

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait);

    public List<TraitUploadReport> createDiseaseTrait(List<DiseaseTrait> diseaseTraits, User user);


    public DiseaseTrait saveDiseaseTrait(String traitId, DiseaseTraitDto diseaseTraitDto, User user);

    public AnalysisCacheDto similaritySearch(List<AnalysisDTO> diseaseTraitAnalysisDTOS, String analysisId, double threshold);

    public DiseaseTrait updateDiseaseTrait(DiseaseTrait diseaseTrait);

    public void deleteDiseaseTrait(List<String> diseaseTraitIds);

    public Optional<DiseaseTrait> getDiseaseTrait(String traitId);

    public Optional<DiseaseTrait> getDiseaseTraitByTraitName(String traitName);

    public Page<DiseaseTrait> getDiseaseTraits(String trait, String studyId, Pageable page);
}
