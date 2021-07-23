package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_DISEASE_TRAITS)
public class DiseaseTraitController {

    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource<DiseaseTraitDto> addDiseaseTraits(@Valid @RequestBody DiseaseTraitDto diseaseTraitDto) {
        DiseaseTrait diseaseTrait = DiseaseTraitDtoAssembler.disassemble(diseaseTraitDto);
        DiseaseTrait diseaseTraitInserted = diseaseTraitService.createDiseaseTrait(diseaseTrait);
        return diseaseTraitDtoAssembler.toResource(diseaseTraitInserted);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{traitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource<DiseaseTraitDto> getDiseaseTrait(@PathVariable String traitId) {
        Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitService.getDiseaseTrait(traitId);
        if(optionalDiseaseTrait.isPresent()){
            DiseaseTrait diseaseTrait = optionalDiseaseTrait.get();
           return diseaseTraitDtoAssembler.toResource(diseaseTrait);
        }
        else{
            throw new EntityNotFoundException("Disease Trait not found"+ traitId);
        }


    }
}
