package uk.ac.ebi.spot.gwas.curation.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.json.patch.JsonPatchPatchConverter;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.rest.dto.ProvenanceDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.service.impl.DiseaseTraitAssemblyService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_DISEASE_TRAITS)
public class DiseaseTraitController {

    private static final Logger log = LoggerFactory.getLogger(DiseaseTraitController.class);


    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;



    @Autowired
    ProvenanceDtoAssembler provenanceDtoAssembler;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<DiseaseTraitDto> addDiseaseTraits(@Valid @RequestBody DiseaseTraitDto diseaseTraitDto, HttpServletRequest request) {
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        DiseaseTrait diseaseTrait = diseaseTraitDtoAssembler.disassemble(diseaseTraitDto);
        diseaseTrait.setCreated(new Provenance(DateTime.now(), user.getId()));
        DiseaseTrait diseaseTraitInserted = diseaseTraitService.createDiseaseTrait(diseaseTrait);
        return diseaseTraitDtoAssembler.toResource(diseaseTraitInserted);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{traitId}",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<DiseaseTraitDto> updateDiseaseTraits(@PathVariable String traitId,@Valid @RequestBody DiseaseTraitDto diseaseTraitDto, HttpServletRequest request) {
        Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitService.getDiseaseTrait(traitId);
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        DiseaseTrait diseaseTraitUpdated = diseaseTraitService.saveDiseaseTrait(traitId, diseaseTraitDto, user);
        return diseaseTraitDtoAssembler.toResource(diseaseTraitUpdated);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{traitIds}")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public void deleteDiseaseTraits(@PathVariable List<String> traitIds) {
        diseaseTraitService.deleteDiseaseTrait(traitIds);
    }




    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{traitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<DiseaseTraitDto> getDiseaseTrait(@PathVariable String traitId) {
        Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitService.getDiseaseTrait(traitId);
        if(optionalDiseaseTrait.isPresent()) {
            DiseaseTrait diseaseTrait = optionalDiseaseTrait.get();
           return diseaseTraitDtoAssembler.toResource(diseaseTrait);
        }
        else{
            throw new EntityNotFoundException("Disease Trait not found"+ traitId);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{traitId}", produces = "application/json-patch+json")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<DiseaseTraitDto> patchDiseaseTrait(@PathVariable String traitId, @RequestBody JsonPatch jsonPatch, HttpServletRequest request)
            throws JsonPatchException, JsonProcessingException {
        log.info("Inside applyPatchToDiseaseTrait()");
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        Optional<DiseaseTrait> optionalDiseaseTrait = diseaseTraitService.getDiseaseTrait(traitId);
        if(optionalDiseaseTrait.isPresent()) {
            DiseaseTraitDto diseaseTraitPatchedDTO =  applyPatchToDiseaseTrait(jsonPatch,
                    diseaseTraitDtoAssembler.assemble(optionalDiseaseTrait.get()));
            DiseaseTrait  diseaseTraitPatched = diseaseTraitDtoAssembler.disassemble(diseaseTraitPatchedDTO);
            diseaseTraitPatched.setCreated(provenanceDtoAssembler.disassemble(diseaseTraitPatchedDTO.getCreated(), user));
            diseaseTraitPatched.setUpdated(new Provenance(DateTime.now(), user.getId()));
            DiseaseTrait diseaseTraitUpdated =  diseaseTraitService.updateDiseaseTrait(diseaseTraitPatched);
            return diseaseTraitDtoAssembler.toResource(diseaseTraitUpdated);
        }else{
            throw new EntityNotFoundException("Disease Trait not found"+ traitId);
        }

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public PagedResources<DiseaseTraitDto> getDiseaseTraits(PagedResourcesAssembler assembler,
                                                            @RequestParam(value = DepositionCurationConstants.PARAM_TRAIT,
                                                                    required = false) String trait,
                                                            @RequestParam(value = DepositionCurationConstants.PARAM_STUDY_ID,
                                                                    required = false) String studyId,
                                                            @SortDefault(sort = "trait", direction = Sort.Direction.DESC)
                                                            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        log.info("Params passed  trait- {}  studyId - {} pageNumber - {} - pagesize- {} ",trait, studyId,
                pageable.getPageNumber(), pageable.getPageSize());
       Page<DiseaseTrait> pagedDiseaseTraits =  diseaseTraitService.getDiseaseTraits( trait, studyId, pageable);

       log.info(" Size of Page is {}",pagedDiseaseTraits.getSize());
        log.info(" Content of Page is {}",pagedDiseaseTraits.getContent());
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                        .methodOn(DiseaseTraitController.class).getDiseaseTraits(assembler, trait, studyId, pageable));
        log.info(" lb is {}",lb);
        log.info(" Proxy prefix is {}",depositionCurationConfig.getProxy_prefix());
       return assembler.toResource(pagedDiseaseTraits, diseaseTraitDtoAssembler,
               new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));
    }

    public DiseaseTraitDto applyPatchToDiseaseTrait(JsonPatch jsonPatch, DiseaseTraitDto diseaseTraitDto ) throws JsonPatchException, JsonProcessingException {
        log.info("Inside applyPatchToDiseaseTrait()");
        JsonNode jsonPatched = jsonPatch.apply(new ObjectMapper().convertValue(diseaseTraitDto , JsonNode.class));
        return new ObjectMapper().treeToValue(jsonPatched, DiseaseTraitDto.class );
    }
}
