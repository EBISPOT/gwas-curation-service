package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;

import java.util.List;
import java.util.Optional;

public interface EfoTraitService {

    EfoTrait createEfoTrait(EfoTrait efoTrait, User user);

    List<TraitUploadReport> createEfoTraits(List<EfoTrait> efoTraits, User user);

    EfoTrait fullyUpdateEfoTrait(String traitId, EfoTraitDto efoTraitDto, User user);

    EfoTrait patchEfoTrait(EfoTrait efoTrait, User user);

    Optional<EfoTrait> getEfoTrait(String traitId);

    Page<EfoTrait> getEfoTraits(String trait, Pageable pageable);

    void deleteEfoTrait(String traitIds);
}
