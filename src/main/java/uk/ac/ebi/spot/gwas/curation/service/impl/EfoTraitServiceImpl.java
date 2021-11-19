package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotCreateTraitWithDuplicateNameException;

import java.util.Optional;

@Service
public class EfoTraitServiceImpl implements EfoTraitService {

    private final EfoTraitRepository efoTraitRepository;

    public EfoTraitServiceImpl(EfoTraitRepository efoTraitRepository) {
        this.efoTraitRepository = efoTraitRepository;
    }

    @Override
    public EfoTrait createDiseaseTrait(EfoTrait efoTrait) {

        EfoTrait efoTraitCreated;
        try {
            efoTraitCreated = efoTraitRepository.insert(efoTrait);
        }
        catch (DuplicateKeyException e) {
            throw new CannotCreateTraitWithDuplicateNameException("Trait name already exists!");
        }
        return efoTraitCreated;
    }

    @Override
    public Optional<EfoTrait> getEfoTrait(String traitId) {

        return efoTraitRepository.findById(traitId);
    }

    @Override
    public Page<EfoTrait> getEfoTraits(String trait, Pageable pageable) {

        if (!StringUtils.isEmpty(trait)) {
            return efoTraitRepository.findByTrait(trait, pageable);
        }
        return efoTraitRepository.findAll(pageable);
    }
}
