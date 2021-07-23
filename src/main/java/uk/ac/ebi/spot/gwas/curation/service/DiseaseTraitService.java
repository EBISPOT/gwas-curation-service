package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;

import java.util.Optional;

public interface DiseaseTraitService {

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait);

    public Optional<DiseaseTrait> getDiseaseTrait(String traitId);
}
