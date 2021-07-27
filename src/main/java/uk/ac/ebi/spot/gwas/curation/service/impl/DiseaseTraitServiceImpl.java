package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;

import java.util.Optional;

@Service
public class DiseaseTraitServiceImpl implements DiseaseTraitService {

    private DiseaseTraitRepository diseaseTraitRepository;

    public DiseaseTraitServiceImpl(DiseaseTraitRepository diseaseTraitRepository) {
        this.diseaseTraitRepository = diseaseTraitRepository;
    }

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait) {

        DiseaseTrait diseaseTraitInserted = diseaseTraitRepository.save(diseaseTrait);
        return diseaseTraitInserted;
    }

    public Optional<DiseaseTrait> getDiseaseTrait(String traitId) {
        return diseaseTraitRepository.findById(traitId);
    }

    public Page<DiseaseTrait> getDiseaseTraits(String trait, String studyId, Pageable page) {
        if(trait !=null && studyId != null)
            return diseaseTraitRepository.findByStudyIdsContainsAndTrait(studyId, trait, page);
        else if(trait != null)
            return diseaseTraitRepository.findByTrait(trait, page);
        else if(studyId != null)
            return diseaseTraitRepository.findByStudyIdsContains(studyId, page);

        return diseaseTraitRepository.findAll(page);
    }
}
