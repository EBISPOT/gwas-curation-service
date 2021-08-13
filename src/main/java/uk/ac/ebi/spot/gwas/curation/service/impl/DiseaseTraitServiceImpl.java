package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.curation.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.exception.CannotDeleteTraitException;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DiseaseTraitServiceImpl implements DiseaseTraitService {

    private static final Logger log = LoggerFactory.getLogger(DiseaseTraitServiceImpl.class);

    private DiseaseTraitRepository diseaseTraitRepository;

    @Autowired
    private StudyRepository studyRepository;

    public DiseaseTraitServiceImpl(DiseaseTraitRepository diseaseTraitRepository) {
        this.diseaseTraitRepository = diseaseTraitRepository;
    }

    public DiseaseTrait createDiseaseTrait(DiseaseTrait diseaseTrait) {

        DiseaseTrait diseaseTraitInserted = diseaseTraitRepository.insert(diseaseTrait);
        return diseaseTraitInserted;
    }


    public List<TraitUploadReport> createDiseaseTrait(List<DiseaseTrait> diseaseTraits,User user) {
        List<TraitUploadReport> report = new ArrayList<>();
        diseaseTraits.forEach(diseaseTrait -> {
            try {
                diseaseTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
                diseaseTraitRepository.insert(diseaseTrait);
                report.add(new TraitUploadReport(diseaseTrait.getTrait(),"Trait successfully Inserted : "+diseaseTrait.getTrait()));
            } catch(DataAccessException ex){
                report.add(new TraitUploadReport(diseaseTrait.getTrait(),"Trait Insertion failed as Trait already exists : "+diseaseTrait.getTrait()));
            }
        });
        //DiseaseTrait diseaseTraitInserted = diseaseTraitRepository.insert(diseaseTrait);
        return report;
    }

    public DiseaseTrait updateDiseaseTrait(DiseaseTrait diseaseTrait) {
        log.info("Inside updateDiseaseTrait()");
        DiseaseTrait diseaseTraitUpdated = diseaseTraitRepository.save(diseaseTrait);
        return diseaseTraitUpdated;
    }

    public void deleteDiseaseTrait(String diseaseTraitIds) {
        List<String> errorTraits = new ArrayList<>();
        List<String> errorStudyTraits = new ArrayList<>();
        if(diseaseTraitIds.contains(",")) {
            String[] traitIds = diseaseTraitIds.split(",");
            Arrays.asList(traitIds).forEach(traitId -> {
                if(!getDiseaseTrait(traitId).isPresent())
                    errorTraits.add(traitId);
                if(!checkForLinkedStudies(traitId))
                    diseaseTraitRepository.deleteById(traitId);
                else
                    errorStudyTraits.add(traitId);
            });
        } else{
            if(!checkForLinkedStudies(diseaseTraitIds))
                diseaseTraitRepository.deleteById(diseaseTraitIds);
            else
                errorStudyTraits.add(diseaseTraitIds);
        }
        String errorTraitsMessage = errorStudyTraits.stream().collect(Collectors.joining(","));
        String errorStudyTraitsMessage = errorStudyTraits.stream().collect(Collectors.joining(","));
        if(!errorTraits.isEmpty())
            throw new EntityNotFoundException("Disease Trait not found:"+errorTraitsMessage);
        if(!errorStudyTraits.isEmpty())
            throw new CannotDeleteTraitException("Can't delete Trait as is linked to a study:"+errorStudyTraitsMessage);
    }

    public boolean checkForLinkedStudies(String traitId) {
         boolean  studyLinkedToTrait = false;
     Optional<DiseaseTrait> optDiseaseTrait = diseaseTraitRepository.findById(traitId);
     if(optDiseaseTrait.isPresent()){
         DiseaseTrait diseaseTrait = optDiseaseTrait.get();
         List<String> studyIds = diseaseTrait.getStudyIds();
         Stream<Study> studyStream = studyRepository.readByIdIn(studyIds);
         if(studyStream != null && studyStream.count() > 0){
             studyLinkedToTrait = true;
         }
     }
        return studyLinkedToTrait;
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
            List<String> studies = diseaseTrait.getStudyIds();
            Optional.ofNullable(diseaseTraitDto.getStudies()).ifPresent(studys -> studys.forEach(studyID -> {
                if (!studies.contains(studyID))
                    studies.add(studyID);
            }));
            diseaseTrait.setUpdated(new Provenance(DateTime.now(), user.getId()));
            return diseaseTraitRepository.save(diseaseTrait);
        } else {
            throw new EntityNotFoundException("Disease Trait Not found");
        }
    }


}
