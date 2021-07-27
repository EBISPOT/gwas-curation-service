package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;

import java.util.Optional;

public interface DiseaseTraitService {

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait);

    public Optional<DiseaseTrait> getDiseaseTrait(String traitId);

    public Page<DiseaseTrait> getDiseaseTraits(String trait, String studyId, Pageable page);
}
