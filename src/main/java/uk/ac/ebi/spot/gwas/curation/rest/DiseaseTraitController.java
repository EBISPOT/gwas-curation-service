package uk.ac.ebi.spot.gwas.curation.rest;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_DISEASE_TRAITS)
public class DiseaseTraitController {

    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource<DiseaseTraitDto> addDiseaseTraits(@Valid @RequestBody DiseaseTraitDto diseaseTraitDto, HttpServletRequest request) {
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        DiseaseTrait diseaseTrait = DiseaseTraitDtoAssembler.disassemble(diseaseTraitDto);
        diseaseTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
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
