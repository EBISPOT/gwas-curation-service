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
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EFOTraitWrapperDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.UploadReportWrapper;
import uk.ac.ebi.spot.gwas.deposition.exception.*;

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

        EfoTrait efoTraitCreated = null;
        try {
            if(validateEFOTraits(efoTrait)) {
                efoTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
                String uri = efoTrait.getUri();
                efoTrait.setShortForm(uri.substring(uri.lastIndexOf('/') + 1));
                efoTraitCreated = efoTraitRepository.insert(efoTrait);
            }
        }
        catch (DuplicateKeyException e) {
            throw new CannotCreateTraitWithDuplicateUriException("Trait with same URI already exists.");
        }
        return efoTraitCreated;
    }

    @Override
    public UploadReportWrapper createEfoTraits(List<EfoTrait> efoTraits, User user) {

        List<TraitUploadReport> report = new ArrayList<>();
        UploadReportWrapper uploadReportWrapper = new UploadReportWrapper();
        efoTraits.forEach(efoTrait -> {
            try {
                if(validateEFOTraits(efoTrait)) {
                    createEfoTrait(efoTrait, user);
                    report.add(new TraitUploadReport(efoTrait.getTrait(), "Trait successfully added : " + efoTrait.getTrait(), null));
                }
            } catch(CannotCreateTraitWithDuplicateUriException ex) {
                uploadReportWrapper.setHasErrors(true);
                report.add(new TraitUploadReport(efoTrait.getTrait(),"Trait cannot be added because one with same URI already exists: " + efoTrait.getTrait(), null));
            } catch(InvalidEFOUriException ex) {
                uploadReportWrapper.setHasErrors(true);
                report.add(new TraitUploadReport(efoTrait.getTrait(),"Trait cannot be added because the URI is not valid: " + efoTrait.getTrait(), null));
            } catch (ConstraintViolationException ex) {
                uploadReportWrapper.setHasErrors(true);
                if (efoTrait.getTrait() == null || efoTrait.getTrait().equals("")) {
                    report.add(new TraitUploadReport("Empty trait field", ex.getMessage(), null));
                }
                else {
                    report.add(new TraitUploadReport(efoTrait.getTrait(), ex.getMessage(), null));
                }
            } catch(CannotCreateTraitWithDuplicateNameException ex){
                uploadReportWrapper.setHasErrors(true);
                report.add(new TraitUploadReport(efoTrait.getTrait(),"Trait cannot be added because the trait name already exists: " + efoTrait.getTrait(), null));
            }
        });
        uploadReportWrapper.setUploadReport(fileHandler.serializePojoToTsv(report));
        return uploadReportWrapper;
    }

    @Override
    public EfoTrait fullyUpdateEfoTrait(String traitId, EfoTraitDto efoTraitDto, User user) {

        Optional<EfoTrait> efoTraitOptional = efoTraitRepository.findById(traitId);
        if (efoTraitOptional.isPresent()) {
            EfoTrait existingTrait = efoTraitOptional.get();
            if(!existingTrait.getTrait().trim().equals(efoTraitDto.getTrait().trim())) {
                List<EfoTrait> existingEfoTraits = efoTraitRepository.findByTraitContainingIgnoreCase(efoTraitDto.getTrait().trim());
                if (existingEfoTraits != null && !existingEfoTraits.isEmpty()) {
                    String existingEFOsMessage = "EFO Traits already exists for trait -> " + existingTrait.getTrait().trim();
                    throw new CannotCreateTraitWithDuplicateNameException(existingEFOsMessage);
                }
            }
            if(!existingTrait.getUri().trim().equals(efoTraitDto.getUri().trim())) {
                List<EfoTrait> existingEfoTraitsUri = efoTraitRepository.findByUri(efoTraitDto.getUri().trim());
                if (existingEfoTraitsUri != null && !existingEfoTraitsUri.isEmpty()) {
                    String existingEFOUriMessage = "EFO trait already exists for " +
                            "the Uri ->" + existingTrait.getUri().trim();
                    throw new CannotCreateTraitWithDuplicateUriException(existingEFOUriMessage);
                }
            }
            if(!CurationUtil.validateURLFormat(efoTraitDto.getUri().trim())) {
                String invalidURIMessage = "The URI value entered \"" + existingTrait.getUri() + "\" is not valid. " +
                        "The URI value should be formatted similar to: http://www.ebi.ac.uk/efo/EFO_1234567.";
                throw new InvalidEFOUriException(invalidURIMessage);
            }
            else if(!CurationUtil.validateCURIEFormat(efoTraitDto.getUri().trim())) {
                String invalidCurieMessage = "The URI value entered \"" + existingTrait.getUri() + "\" is not valid. " +
                        "The URI value for OBO Foundry ontologies should be formatted similar " +
                        "to: http://www.ebi.ac.uk/efo/EFO_1234567. \n Did you copy-paste the entire URI?";
                throw new InvalidEFOUriException(invalidCurieMessage);
            }

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


    public Boolean validateEFOTraits(EfoTrait efoTrait) {

        List<EfoTrait> existingEfoTraits = efoTraitRepository.findByTraitContainingIgnoreCase(efoTrait.getTrait().trim());
        List<EfoTrait> existingEfoTraitsUri = efoTraitRepository.findByUri(efoTrait.getUri().trim());
        if(!CurationUtil.validateURLFormat(efoTrait.getUri().trim())) {
            String invalidURIMessage = "The URI value entered \"" + efoTrait.getUri() + "\" is not valid. " +
                    "The URI value should be formatted similar to: http://www.ebi.ac.uk/efo/EFO_1234567.";
            throw new InvalidEFOUriException(invalidURIMessage);
        }
        else if(!CurationUtil.validateCURIEFormat(efoTrait.getUri().trim())) {
            String invalidCurieMessage = "The URI value entered \"" + efoTrait.getUri() + "\" is not valid. " +
                    "The URI value for OBO Foundry ontologies should be formatted similar " +
                    "to: http://www.ebi.ac.uk/efo/EFO_1234567. \n Did you copy-paste the entire URI?";
            throw new InvalidEFOUriException(invalidCurieMessage);
        }
        else if( existingEfoTraits != null && !existingEfoTraits.isEmpty()) {
            String existingEFOsMessage = "EFO Traits already exists for trait -> "+efoTrait.getTrait().trim();
            throw new CannotCreateTraitWithDuplicateNameException(existingEFOsMessage);
        }
        else if(existingEfoTraitsUri != null && !existingEfoTraitsUri.isEmpty()) {
            String existingEFOUriMessage = "EFO trait already exists for " +
                    "the Uri ->"+efoTrait.getUri().trim();
            throw new CannotCreateTraitWithDuplicateUriException(existingEFOUriMessage);
        }
        return true;
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
