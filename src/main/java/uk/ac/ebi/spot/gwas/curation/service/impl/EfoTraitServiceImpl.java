package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.EfoTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.EfoTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EFOTraitWrapperDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotCreateTraitWithDuplicateUriException;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotDeleteTraitException;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EfoTraitServiceImpl implements EfoTraitService {

    private final EfoTraitRepository efoTraitRepository;

    private final StudyRepository studyRepository;

    private final EfoTraitDtoAssembler efoTraitDtoAssembler;

    private final FileHandler fileHandler;

    public EfoTraitServiceImpl(EfoTraitRepository efoTraitRepository, StudyRepository studyRepository, EfoTraitDtoAssembler efoTraitDtoAssembler, FileHandler fileHandler) {
        this.efoTraitRepository = efoTraitRepository;
        this.studyRepository = studyRepository;
        this.efoTraitDtoAssembler = efoTraitDtoAssembler;
        this.fileHandler = fileHandler;
    }

    @Override
    public EfoTrait createEfoTrait(EfoTrait efoTrait, User user) {

        EfoTrait efoTraitCreated;
        try {
            efoTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
            String uri = efoTrait.getUri();
            efoTrait.setShortForm(uri.substring(uri.lastIndexOf('/') + 1));
            efoTraitCreated = efoTraitRepository.insert(efoTrait);
        }
        catch (DuplicateKeyException e) {
            throw new CannotCreateTraitWithDuplicateUriException("Trait with same URI already exists.");
        }
        return efoTraitCreated;
    }

    @Override
    public byte[] createEfoTraits(List<EfoTrait> efoTraits, User user) {

        List<TraitUploadReport> report = new ArrayList<>();
        efoTraits.forEach(efoTrait -> {
            try {
                createEfoTrait(efoTrait, user);
                report.add(new TraitUploadReport(efoTrait.getTrait(),"Trait successfully added : " + efoTrait.getTrait(), null));

            }
            catch(CannotCreateTraitWithDuplicateUriException ex) {
                report.add(new TraitUploadReport(efoTrait.getTrait(),"Trait cannot be added because one with same URI already exists: " + efoTrait.getTrait(), null));
            }
            catch (ConstraintViolationException ex) {
                if (efoTrait.getTrait() == null || efoTrait.getTrait().equals("")) {
                    report.add(new TraitUploadReport("Empty trait field", ex.getMessage(), null));
                }
                else {
                    report.add(new TraitUploadReport(efoTrait.getTrait(), ex.getMessage(), null));
                }
            }
        });
        return fileHandler.serializePojoToTsv(report);
    }

    @Override
    public EfoTrait fullyUpdateEfoTrait(String traitId, EfoTraitDto efoTraitDto, User user) {

        Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findById(traitId);
        if (efoTraitOptional.isPresent()) {
            EfoTrait updatedEfoTrait = efoTraitDtoAssembler.disassemble(efoTraitDto);
            updatedEfoTrait.setId(traitId);
            String uri = updatedEfoTrait.getUri();
            updatedEfoTrait.setShortForm(uri.substring(uri.lastIndexOf('/') + 1));
            updatedEfoTrait.setCreated(efoTraitOptional.get().getCreated());
            updatedEfoTrait.setUpdated(new Provenance(DateTime.now(), user.getId()));
            try {
                return efoTraitRepository.save(updatedEfoTrait);
            }
            catch (DuplicateKeyException e) {
                throw new CannotCreateTraitWithDuplicateUriException("Trait with same URI already exists.");
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
            return efoTraitRepository.findByTraitContainingIgnoreCase(trait, pageable);
        }
        return efoTraitRepository.findAll(pageable);
    }

    @Override
    public List<EfoTrait> getEfoTraits(String trait) {

        if (!StringUtils.isEmpty(trait)) {
            return efoTraitRepository.findByTraitContainingIgnoreCase(trait);
        }
        return efoTraitRepository.findAll();
    }

    @Override
    public byte[] getEfoTraitsTsv(String trait) {

        List<EfoTrait> efoTraits = getEfoTraits(trait);
        List<EFOTraitWrapperDTO> efoTraitWrapperDtos = efoTraits
                .stream()
                .map(efoTrait -> EFOTraitWrapperDTO.builder().trait(efoTrait.getTrait()).uri(efoTrait.getUri()).build())
                .collect(Collectors.toList());
        return fileHandler.serializePojoToTsv(efoTraitWrapperDtos);
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
            else if (studyRepository.findByEfoTraitsContains(traitIds).findAny().isPresent()) {
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
