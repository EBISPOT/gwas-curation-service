package uk.ac.ebi.spot.gwas.curation.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.constants.FileUploadType;
import uk.ac.ebi.spot.gwas.curation.rest.dto.EfoTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.EfoTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.FileUploadRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.deposition.exception.FileValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_EFO_TRAITS)
public class EfoTraitController {

    private static final Logger log = LoggerFactory.getLogger(EfoTraitController.class);

    private final EfoTraitService efoTraitService;

    private final EfoTraitDtoAssembler efoTraitDtoAssembler;

    private final DepositionCurationConfig depositionCurationConfig;

    private final UserService userService;

    private final JWTService jwtService;

    public EfoTraitController(EfoTraitService efoTraitService, EfoTraitDtoAssembler efoTraitDtoAssembler,
                              DepositionCurationConfig depositionCurationConfig, UserService userService,
                              JWTService jwtService) {
        this.efoTraitService = efoTraitService;
        this.efoTraitDtoAssembler = efoTraitDtoAssembler;
        this.depositionCurationConfig = depositionCurationConfig;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resource<EfoTraitDto> createEfoTrait(@Valid @RequestBody EfoTraitDto efoTraitDto, HttpServletRequest request) {

        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        EfoTrait efoTrait = efoTraitDtoAssembler.disassemble(efoTraitDto);
        EfoTrait efoTraitCreated = efoTraitService.createEfoTrait(efoTrait, user);
        return efoTraitDtoAssembler.toResource(efoTraitCreated);
    }

    @GetMapping(value = "/download-template", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> downloadCreationTemplate() {

        byte[] result = Objects.requireNonNull(FileHandler.getTemplate(FileUploadType.EFO_TRAIT_FILE)).getBytes();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + FileUploadType.EFO_TRAIT_FILE + ".tsv");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Integer.toString(result.length));
        return new HttpEntity<>(result, responseHeaders);
    }

    @PostMapping(value = "/bulk-upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<TraitUploadReport>> createEfoTraits(@Valid FileUploadRequest fileUploadRequest,
                                                                   HttpServletRequest request, BindingResult result) {

        if (result.hasErrors()) {
            throw new FileValidationException(result);
        }
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        MultipartFile multipartFile = fileUploadRequest.getMultipartFile();
        List<EfoTrait> efoTraits = efoTraitDtoAssembler.disassemble(multipartFile);
        List<TraitUploadReport> traitUploadReports = efoTraitService.createEfoTraits(efoTraits, user);
        return new ResponseEntity<>(traitUploadReports, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{traitId}")
    @ResponseStatus(HttpStatus.OK)
    public Resource<EfoTraitDto> getEfoTrait(@PathVariable String traitId) {

        Optional<EfoTrait> efoTraitOptional = efoTraitService.getEfoTrait(traitId);
        if (efoTraitOptional.isPresent()) {
            EfoTrait efoTrait = efoTraitOptional.get();
            return efoTraitDtoAssembler.toResource(efoTrait);
        }
        else {
            throw new EntityNotFoundException("Efo trait not found" + traitId);
        }
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedResources<EfoTraitDto> getEfoTraits(
            PagedResourcesAssembler assembler,
            @RequestParam(value = DepositionCurationConstants.PARAM_TRAIT, required = false) String trait,
            @SortDefault(sort = "trait", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Params passed  trait - {} pageNumber - {} - pageSize- {} ", trait,
                pageable.getPageNumber(), pageable.getPageSize());
        Page<EfoTrait> efoTraitPage =  efoTraitService.getEfoTraits(trait, pageable);

        log.info("Size of Page is {}", efoTraitPage.getSize());
        log.info("Content of Page is {}", efoTraitPage.getContent());
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(EfoTraitController.class).getEfoTraits(assembler, trait, pageable));
        log.info("lb is {}", lb);
        log.info("Proxy prefix is {}", depositionCurationConfig.getProxy_prefix());
        return assembler.toResource(efoTraitPage, efoTraitDtoAssembler,
                new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));
    }
    
    @DeleteMapping(value = "/{traitIds}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEfoTrait(@PathVariable String traitIds, HttpServletRequest request) {
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        efoTraitService.deleteEfoTrait(traitIds);
    }

    @PutMapping(value = "/{traitId}")
    @ResponseStatus(HttpStatus.OK)
    public Resource<EfoTraitDto> fullyUpdateEfoTrait(@PathVariable String traitId, @RequestBody EfoTraitDto efoTraitDto, HttpServletRequest request) {

        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        return efoTraitDtoAssembler.toResource(efoTraitService.fullyUpdateEfoTrait(traitId, efoTraitDto, user));
    }
}
