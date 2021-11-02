package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;

import java.util.Optional;

public interface EfoTraitService {

    EfoTrait createDiseaseTrait(EfoTrait efoTrait);

    Optional<EfoTrait> getEfoTrait(String traitId);

    Page<EfoTrait> getEfoTraits(String trait, Pageable pageable);
}
