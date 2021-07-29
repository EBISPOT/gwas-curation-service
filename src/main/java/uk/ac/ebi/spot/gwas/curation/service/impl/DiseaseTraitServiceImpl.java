package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class DiseaseTraitServiceImpl implements DiseaseTraitService {

    private DiseaseTraitRepository diseaseTraitRepository;

    public DiseaseTraitServiceImpl(DiseaseTraitRepository diseaseTraitRepository) {
        this.diseaseTraitRepository = diseaseTraitRepository;
    }

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait) {

        DiseaseTrait diseaseTraitInserted = diseaseTraitRepository.insert(diseaseTrait);
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

    public DiseaseTrait saveDiseaseTrait(String traitId, DiseaseTraitDto diseaseTraitDto, User user) {
        Optional<DiseaseTrait> optDiseaseTrait = this.getDiseaseTrait(traitId);
        if (optDiseaseTrait.isPresent()) {
            DiseaseTrait diseaseTrait = optDiseaseTrait.get();
            Optional.ofNullable(diseaseTraitDto.getTrait()).ifPresent(trait -> diseaseTrait.setTrait(diseaseTraitDto.getTrait()));
            diseaseTrait.setTrait(diseaseTraitDto.getTrait());
            List<String> studies = diseaseTrait.getStudyIds();
            diseaseTraitDto.getStudies().forEach(studyID -> {
                if (!studies.contains(studyID))
                    studies.add(studyID);
            });
            diseaseTrait.setUpdated(new Provenance(DateTime.now(), user.getId()));
            return diseaseTraitRepository.save(diseaseTrait);
        } else {
            throw new EntityNotFoundException("Disease Trait Not found");
        }
    }


}
