package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.EfoTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotCreateTraitWithDuplicateNameException;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotDeleteTraitException;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EfoTraitServiceImpl implements EfoTraitService {

    private final EfoTraitRepository efoTraitRepository;

    private final StudyRepository studyRepository;

    private final EfoTraitDtoAssembler efoTraitDtoAssembler;

    public EfoTraitServiceImpl(EfoTraitRepository efoTraitRepository, StudyRepository studyRepository, EfoTraitDtoAssembler efoTraitDtoAssembler) {
        this.efoTraitRepository = efoTraitRepository;
        this.studyRepository = studyRepository;
        this.efoTraitDtoAssembler = efoTraitDtoAssembler;
    }

    @Override
    public EfoTrait createEfoTrait(EfoTrait efoTrait, User user) {

        EfoTrait efoTraitCreated;
        try {
            efoTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
            efoTraitCreated = efoTraitRepository.insert(efoTrait);
        }
        catch (DuplicateKeyException e) {
            throw new CannotCreateTraitWithDuplicateNameException("Trait name already exists!");
        }
        return efoTraitCreated;
    }

    @Override
    public List<TraitUploadReport> createEfoTraits(List<EfoTrait> efoTraits, User user) {

        List<TraitUploadReport> report = new ArrayList<>();
        efoTraits.forEach(efoTrait -> {
            try {
                createEfoTrait(efoTrait, user);
                report.add(new TraitUploadReport(efoTrait.getTrait(),"Trait successfully Inserted : " + efoTrait.getTrait()));
            }
            catch(DataAccessException ex){
                report.add(new TraitUploadReport(efoTrait.getTrait(),"Trait Insertion failed as Trait already exists : " + efoTrait.getTrait()));
            }
        });
        return report;
    }

    @Override
    public EfoTrait fullyUpdateEfoTrait(String traitId, EfoTraitDto efoTraitDto, User user) {

        Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findById(traitId);
        if (efoTraitOptional.isPresent()) {
            EfoTrait updatedEfoTrait = efoTraitDtoAssembler.disassemble(efoTraitDto);
            updatedEfoTrait.setId(traitId);
            updatedEfoTrait.setCreated(efoTraitOptional.get().getCreated());
            updatedEfoTrait.setUpdated(new Provenance(DateTime.now(), user.getId()));
            try {
                return efoTraitRepository.save(updatedEfoTrait);
            }
            catch (DuplicateKeyException e) {
                throw new CannotCreateTraitWithDuplicateNameException("Trait name already exists!");
            }
        }
        throw new EntityNotFoundException("EFO Trait with id " + traitId + " not found.");
    }

    @Override
    public EfoTrait patchEfoTrait(EfoTrait efoTrait, User user) {

        return null;
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

    @Override
    public void deleteEfoTrait(String traitIds) {

        String[] traitIdsArray = traitIds.split(",");
        List<String> notFoundTraits = new ArrayList<>();
        List<String> assignedToStudyTraits = new ArrayList<>();
        for (String traitId: traitIdsArray) {
            if (!efoTraitRepository.existsById(traitId)) {
                notFoundTraits.add(traitId);
            }
            else if (studyRepository.findByEfoTraitListContains(traitIds).findAny().isPresent()) {
                assignedToStudyTraits.add(traitId);
            }
            else {
                efoTraitRepository.deleteById(traitId);
            }
        }
        if (!notFoundTraits.isEmpty()) {
            throw new EntityNotFoundException("EFO traits with ids " + notFoundTraits + " not found.");
        }
        if (!assignedToStudyTraits.isEmpty()) {
            throw new CannotDeleteTraitException("Unable to delete EFO trait " + assignedToStudyTraits + " as they are linked to studies.");
        }
    }
}
